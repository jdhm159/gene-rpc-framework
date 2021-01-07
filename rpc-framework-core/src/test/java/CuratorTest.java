import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.extension.ExtensionLoader;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.registry.ServiceDiscovery;
import github.genelin.registry.ServiceRegistry;
import github.genelin.registry.zookeeper.listener.ServiceRegistryConnectionListener;
import github.genelin.registry.zookeeper.util.CuratorUtils;
import github.genelin.remoting.constants.RpcConstants;
import github.genelin.remoting.transport.ServiceProviderImpl;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
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
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import service.HelloService;
import service.HelloServiceImpl1;
import service.HelloServiceImpl2;

/**
 * @author gene lin
 * @createTime 2020/12/25 14:36
 */
@Slf4j
public class CuratorTest {


    @Test
    public void createClient1() throws InterruptedException {
        CuratorFramework client = CuratorUtils.getClient();
        CuratorFramework c = CuratorUtils.getClient();
        Assert.assertEquals(client, c);
        Assert.assertFalse(CuratorUtils.isConnecting());
        client.start();
        Assert.assertTrue(CuratorUtils.isConnecting());
    }


    @Test
    public void createEphemeralNode() throws UnknownHostException {
        CuratorFramework client = CuratorUtils.getClient();
        client.start();
        String name = RpcServiceProperties.builder().interfaceName("gene.TestService").build().toRPCServiceName();
        InetSocketAddress url = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress()
            , RpcConstants.DEFAULT_PORT);
        CuratorUtils.createEphemeralNode(name, url);
        // result：
        // path = /gene-rpc/gene.TestService/p0000000001  ，value = /localhost:22159
    }

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

    @Test
    public void registerWatcherTest() throws InterruptedException {
        CuratorFramework client = CuratorUtils.getClient();
        client.start();

        List<String> urlList = new ArrayList<>();
        CuratorUtils.registerWatcher("HelloService",urlList);

        Thread.sleep(1000);
        Assert.assertEquals(urlList.size(),2);

        CuratorUtils.createEphemeralNode("HelloService", new InetSocketAddress("127.0.0.1",22125));

        Thread.sleep(1000);
        Assert.assertEquals(urlList.size(),3);

        for (int i = 0; i < urlList.size(); i++) {
            log.info(urlList.get(i));
        }

    }

    @Test
    public void createClientForServiceRegistry() throws InterruptedException {
        // 由ServiceProvider进行获取
        ServiceProviderImpl provider = SingletonFactory.getSingletonObject(ServiceProviderImpl.class);
        provider.registerService(new HelloServiceImpl1());
        RpcServiceProperties properties = RpcServiceProperties.builder().interfaceName(HelloService.class.getName()).group("impl2").build();
        provider.registerService(properties, new HelloServiceImpl2());
        provider.publishServices();

    }

    @Test
    public void createClientForServiceDiscovery() throws InterruptedException {
        ServiceDiscovery serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getDefaultExtension();
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().interfaceName(HelloService.class.getName()).build();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceProperties);
        log.info(String.valueOf(inetSocketAddress));
    }


}
