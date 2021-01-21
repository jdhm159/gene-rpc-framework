package github.genelin.registry.zookeeper.util;

import github.genelin.common.enums.RpcConfigEnum;
import github.genelin.common.entity.Holder;
import github.genelin.common.util.PropertiesFileUtils;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.imps.GzipCompressionProvider;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;

/**
 * @author gene lin
 * @createTime 2020/12/23 16:36
 */
@Slf4j
public class CuratorUtils {

    private CuratorUtils() {
    }

    public static final String DEFAULT_NAMESPACE = "gene-rpc";

    // retryPolicy相关
    public static final int BASE_SLEEP_TIME_MS = 50;
    public static final int MAX_RETRIES = 10;
    public static final int MAX_SLEEP_MS = 500;

    private static final String connectString;
    private static final String nameSpace;
    // 每个RPCClient/RPCServer复用一个Zookeeper客户端连接，该实例是线程安全的
    private static volatile CuratorFramework zkClient;

    private static final ConcurrentHashMap<String, Holder<PathChildrenCache>> pathChildrenCaches = new ConcurrentHashMap<>();

    private static final Set<String> registeredNodeNames = new CopyOnWriteArraySet<>();

    static {
        Properties rpcConfig = PropertiesFileUtils.getPropertiesByFileName(RpcConfigEnum.RPC_CONFIG_FILENAME.getPropertyName());
        connectString = rpcConfig.getProperty(RpcConfigEnum.ZOOKEEPER_SERVER_URL.getPropertyName());
        String rootPath = rpcConfig.getProperty(RpcConfigEnum.ZOOKEEPER_ROOT_PATH.getPropertyName());
        nameSpace = rootPath == null ? DEFAULT_NAMESPACE : rootPath;
        // digest信息读取...
    }


    public static boolean isConnecting() {
        return zkClient != null && zkClient.getState().equals(CuratorFrameworkState.STARTED);
    }

    /**
     * 返回zk客户端实例（未start）
     */
    public static CuratorFramework getClient() {
        if (zkClient == null) {
            synchronized (CuratorUtils.class) {
                if (zkClient == null) {
                    zkClient = CuratorFrameworkFactory.builder()
                        .connectString(connectString)
                        // 超时重试策略
                        .retryPolicy(buildRetryPolicy())
                        .namespace(nameSpace)
                        .compressionProvider(new GzipCompressionProvider())
                        .build();
                    log.info("Curator[zookeeper] client created successfully");
                }
            }
        }
        return zkClient;
    }

//     后续再实现 digest
//    public static void createClient(String connectionString, List<AuthInfo> auths){
//        zkClient = CuratorFrameworkFactory.builder()
//            .connectString(connectionString)
//            // 超时重试策略
//            .retryPolicy(new ExponentialBackoffRetry(1000, 3))
//            .namespace(NAMESPACE)
//            .compressionProvider(new GzipCompressionProvider())
//            .authorization(auths)
//            .build();
//        return zkClient;
//    }

    /**
     * 创建临时有序节点 节点路径：/namespace/rpcServiceNameXXX 如：/gene-rpc/github.gene.service.HelloService/p0000000000   value = 172.26.152.177:22159
     *
     * @param rpcServiceName 服务标识名
     */
    public static void createEphemeralNode(String rpcServiceName, InetSocketAddress url) {
        String createdNodeName = null;
        try {
            createdNodeName = getClient().create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .withACL(Ids.READ_ACL_UNSAFE)
                .forPath(buildChildPath(rpcServiceName), url.toString().getBytes());
            log.info("Service publishing: the Node [{}] created successfully", rpcServiceName);
        } catch (Exception e) {
            log.error("Service publish failure: when creating the node [{}]", rpcServiceName, e);
        }
        registeredNodeNames.add(createdNodeName);
    }

    /**
     * 使用PathChildrenCache注册监听事件，借助Cache同步初始化同时初始化本地缓存list
     */
    public static void registerWatcher(String rpcServiceName, List<String> providers) {
        Holder<PathChildrenCache> cacheHolder = pathChildrenCaches.get(rpcServiceName);
        if (cacheHolder == null) {
            pathChildrenCaches.putIfAbsent(rpcServiceName, new Holder<>());
            cacheHolder = pathChildrenCaches.get(rpcServiceName);
        }
        PathChildrenCache pathChildCache = cacheHolder.get();
        // double check lock, using holder as the object lock
        if (pathChildCache == null) {
            synchronized (cacheHolder) {
                pathChildCache = cacheHolder.get();
                if (pathChildCache == null) {
                    pathChildCache = createPathChildrenCache(buildPath(rpcServiceName), providers);
                    cacheHolder.set(pathChildCache);
                }
            }
        }
    }

    /**
     * 程序正常退出时，清理本地registry在注册中心注册的临时节点
     */
    public static void clearRegistry(){
        log.info("Clear all registered node...");
        for (String registeredNodeName : registeredNodeNames) {
            try {
                getClient().delete().forPath(registeredNodeName);
            } catch (Exception e) {
                log.error("Fail to delete Registered Node[{}]", registeredNodeName);
            }
        }
        getClient().close();
    }

    public static void clearDiscoveryCache() {
        for (Holder<PathChildrenCache> childrenCacheHolder : pathChildrenCaches.values()) {
            PathChildrenCache childrenCache = childrenCacheHolder.get();
            if (childrenCache != null) {
                try {
                    childrenCache.close();
                } catch (IOException e) {
                    log.error("Fail to close pathChildrenCache");
                }
            }
        }
        pathChildrenCaches.clear();
    }

    public static void clearRegistryCache(){
        registeredNodeNames.clear();
    }

    private static PathChildrenCache createPathChildrenCache(String path, List<String> providers) {
        log.info("Watcher注册：创建pathChildrenCache监听于path[{}]", path);
        // 建议传入自己构建的线程池，保证线程可控
        PathChildrenCache pathChildCache = new PathChildrenCache(getClient(), path, true);
        PathChildrenCacheListener l = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) {
                ChildData data = event.getData();
                switch (event.getType()) {
                    case CHILD_ADDED:
                        log.info("Child node added to path of the rpc service[{}]", path);
                        providers.add(new String(data.getData()));
                        break;
                    case CHILD_REMOVED:
                        log.info("Child node remove from path of the rpc service[{}]", path);
                        providers.remove(new String(data.getData()));
                        break;
                    default:
                        break;
                }
            }
        };
        pathChildCache.getListenable().addListener(l);
        try {
            // 同步初始化Cache
            // 会阻塞等待session的重新建立
            pathChildCache.start(StartMode.BUILD_INITIAL_CACHE);
            log.info("Watcher注册：成功同步初始化 pathChildrenCache 及 本地服务清单缓存");
        } catch (Exception e) {
            log.error("Watcher注册: PathCache监听失败 rpcServiceName[{}]", path, e);
        }
        return pathChildCache;
    }

    private static String buildChildPath(String rpcServiceName) {
        return buildPath(rpcServiceName) + "/p";
    }

    private static String buildPath(String rpcServiceName) {
        return "/" + rpcServiceName;
    }

    private static RetryPolicy buildRetryPolicy() {
        return new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES, MAX_SLEEP_MS);
    }

}
