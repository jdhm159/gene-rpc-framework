package github.genelin.registry.zookeeper.util;

import github.genelin.common.enums.RpcConfigEnum;
import github.genelin.common.util.PropertiesFileUtils;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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

    // 每个RPCClient/RPCServer复用一个Zookeeper客户端连接，该实例是线程安全的
    private static volatile CuratorFramework zkClient;
    private static final String connectString;
    private static final String nameSpace;

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

    public static void closeSession() {
        getClient().close();
    }

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
    public static String createEphemeralNode(String rpcServiceName, InetSocketAddress url) {
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
        return createdNodeName;
    }

    /**
     * 获得服务清单 并 注册watcher
     *
     * @param rpcServiceName 服务标识
     * @return 服务清单（URL list）
     */
    public static List<String> getChildNodeList(String rpcServiceName, Map<String, List<String>> serviceList) {
        String path = buildPath(rpcServiceName);
        List<String> nodes = null;
        try {
            nodes = getClient().getChildren().forPath(path);
            registerWatcher(rpcServiceName, serviceList.get(rpcServiceName));
        } catch (Exception e) {
            log.error("Service discovery failure: try to get children nodes of path [{}]", path, e);
        }
        return nodes;
    }

    public static byte[] getNodeData(String path) {
        try {
            return getClient().getData().forPath(path);
        } catch (Exception e) {
            log.error("Service discovery failure: try to get data of node [{}]", path, e);
        }
        return new byte[0];
    }

    public static void registerWatcher(String rpcServiceName, List<String> providers) {
        PathChildrenCache childrenCache = new PathChildrenCache(getClient(), rpcServiceName, true);
        PathChildrenCacheListener l = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) {
                ChildData data = event.getData();
                switch (event.getType()) {
                    case CHILD_ADDED:
                        handleChildAddEvent(rpcServiceName, providers, data);
                        break;
                    case CHILD_REMOVED:
                        handleChildRemoveEvent(rpcServiceName, providers, data);
                        break;
                    default:
                        break;
                }
            }
        };
        childrenCache.getListenable().addListener(l);
        try {
            // 同步初始化Cache
            childrenCache.start(StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            log.error("Watcher注册: PathCache监听失败 rpcServiceName[{}]", rpcServiceName);
        }
    }

    private static void handleChildAddEvent(String rpcServiceName, List<String> providers, ChildData data) {
        log.info("Child node added to path of the rpc service[{}]", rpcServiceName);
        synchronized (providers) {
            providers.add(new String(data.getData()));
        }
    }

    private static void handleChildRemoveEvent(String rpcServiceName, List<String> providers, ChildData data) {
        log.info("Child node remove from path of the rpc service[{}]", rpcServiceName);
        synchronized (providers) {
            providers.remove(new String(data.getData()));
        }
    }

    private static String buildChildPath(String rpcServiceName) {
        return buildPath(rpcServiceName) + "/p";
    }

    public static String buildPath(String rpcServiceName) {
        return "/" + rpcServiceName;
    }

    private static RetryPolicy buildRetryPolicy() {
        return new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES, MAX_SLEEP_MS);
    }

}
