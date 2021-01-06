import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.registry.zookeeper.listener.ServiceRegistryConnectionListener;
import github.genelin.registry.zookeeper.util.CuratorUtils;
import github.genelin.remoting.constants.RpcConstants;
import java.io.*;
import java.net.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.junit.jupiter.api.Test;

/**
 * @author gene lin
 * @createTime 2020/12/25 14:36
 */
@Slf4j
public class CuratorTest {


//    @Test
//    public void createClient() {
//        CuratorFramework client = CuratorUtils.getClient(new ServiceRegistryConnectionListener());
//        log.info("" + CuratorUtils.isConnecting());
//    }
//
//    @Test
//    public void createEphemeralNode() throws UnknownHostException {
//        CuratorUtils.getClient(new ServiceRegistryConnectionListener());
//        String name = RpcServiceProperties.builder().interfaceName("gene.TestService").build().toRPCServiceName();
//        InetSocketAddress url = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress()
//            , RpcConstants.DEFAULT_PORT);
////        InetSocketAddress url = new InetSocketAddress(InetAddress.getLocalHost(), RpcConstants.DEFAULT_PORT);
//        CuratorUtils.createEphemeralNode(name, url);
//    }
//
//    @Test
//    public void getChildrenNode() throws UnknownHostException {
//        CuratorUtils.getClient(new ServiceRegistryConnectionListener());
//        String name = RpcServiceProperties.builder().interfaceName("gene.TestService").build().toRPCServiceName();
//        String hostAddress = InetAddress.getLocalHost().getHostAddress();
//        InetSocketAddress url1 = new InetSocketAddress(hostAddress
//            , RpcConstants.DEFAULT_PORT);
//        InetSocketAddress url2 = new InetSocketAddress("127.0.0.1", 21255);
//        CuratorUtils.createEphemeralNode(name, url1);
//        CuratorUtils.createEphemeralNode(name, url2);
//
//        System.out.println(hostAddress);
//        System.out.println(url1);
//        System.out.println(url2);
//
//        System.out.println(CuratorUtils.getChildNodeList(name));
//    }
//
//    @Test
//    public void StringTest() throws UnknownHostException {
//        System.out.println(InetAddress.getLocalHost());
//        System.out.println(InetAddress.getLocalHost().getHostName());
//        System.out.println(InetAddress.getLocalHost().getHostAddress());
//
//        InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), RpcConstants.DEFAULT_PORT);
//        String[] ipAndPort = inetSocketAddress.toString().split(":");
//        InetSocketAddress inetSocketAddress1 = new InetSocketAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
//
//        System.out.println(ipAndPort[0]);
//        System.out.println(ipAndPort[1]);
//        System.out.println(inetSocketAddress);
//        System.out.println(inetSocketAddress1);
//    }


    @Test
    public void testPathChildrenCache() {

        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("119.45.226.72:2181")
            .retryPolicy(new ExponentialBackoffRetry(3*1000, 8))
            .build();
        client.start();

        try {
            PathChildrenCache cache =
                new PathChildrenCache(client, "/test", true);
            PathChildrenCacheListener l =
                new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework client,
                        PathChildrenCacheEvent event) {
                        try {
                            ChildData data = event.getData();
                            switch (event.getType()) {
                                case CHILD_ADDED:

                                    log.info("子节点增加, path={}, data={}",
                                        data.getPath(), new String(data.getData(), "UTF-8"));

                                    break;
                                case CHILD_UPDATED:
                                    log.info("子节点更新, path={}, data={}",
                                        data.getPath(), new String(data.getData(), "UTF-8"));
                                    break;
                                case CHILD_REMOVED:
                                    log.info("子节点删除, path={}, data={}",
                                        data.getPath(), new String(data.getData(), "UTF-8"));
                                    break;
                                default:
                                    break;
                            }

                        } catch (
                            UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                };
            cache.getListenable().addListener(l);
            cache.start(StartMode.POST_INITIALIZED_EVENT);
            System.out.println("before sleep");
            Thread.sleep(1000);
            System.out.println("after sleep");
            for (int i = 0; i < 3; i++) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/test/p");
            }
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
