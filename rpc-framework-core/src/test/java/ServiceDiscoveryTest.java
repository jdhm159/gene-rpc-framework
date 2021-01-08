import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.extension.ExtensionLoader;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.registry.ServiceDiscovery;
import github.genelin.remoting.transport.ServiceProviderImpl;
import java.net.*;
import org.junit.jupiter.api.Test;
import service.HelloService;
import service.HelloServiceImpl1;
import service.HelloServiceImpl2;

/**
 * @author gene lin
 * @createTime 2021/1/8 16:19
 */
public class ServiceDiscoveryTest {

    @Test
    public void testLookupService() {
        ServiceDiscovery serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getDefaultExtension();
        RpcServiceProperties properties = RpcServiceProperties.builder().interfaceName(HelloService.class.getName()).group("impl2").build();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(properties);
        System.out.println(inetSocketAddress);
    }

    @Test
    public void testWatcher() throws InterruptedException {
        ServiceDiscovery serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getDefaultExtension();
        RpcServiceProperties properties = RpcServiceProperties.builder().interfaceName(HelloService.class.getName()).group("impl2").build();
        for (; ; ) {
            System.out.println(serviceDiscovery.lookupService(properties));
            Thread.sleep(20 * 1000);
        }

        // lookup后注册了watcher
        // 此时server断连
        // 会监听到节点丢失，使得服务清单更改 没有service，为null
    }

    @Test
    public void testLoseConnection() throws InterruptedException {
        ServiceDiscovery serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getDefaultExtension();
        RpcServiceProperties properties = RpcServiceProperties.builder().interfaceName(HelloService.class.getName()).group("impl2").build();
        System.out.println(serviceDiscovery.lookupService(properties));
        while (true);

        // server断连后，自动进行重连
    }

    @Test
    public void testConnectionReconnect() throws InterruptedException {
        ServiceDiscovery serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getDefaultExtension();
        RpcServiceProperties properties = RpcServiceProperties.builder().interfaceName(HelloService.class.getName()).group("impl2").build();
        for (; ; ) {
            System.out.println(serviceDiscovery.lookupService(properties));
            Thread.sleep(20 * 1000);
        }

        // 预期：连接断开后 按照retrypolicy进行重试，会话过期前（connectionstate.loss）开始重新连接，watcher不会丢失，还能监听到子节点的增删
    }

    @Test
    public void testSessionReconnect() throws InterruptedException {
        ServiceDiscovery serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getDefaultExtension();
        RpcServiceProperties properties = RpcServiceProperties.builder().interfaceName(HelloService.class.getName()).group("impl2").build();
        for (; ; ) {
            System.out.println(serviceDiscovery.lookupService(properties));
            Thread.sleep(2 * 1000);
        }

        // 预期：连接过期后 按照retrypolicy进行重试，会话过期后（connectionstate.loss）开始重新连接，watcher（pathChildrenCache）被主动清理，等待下次lookup时再进行注册
        // 预期外现象：1)对于同一个service path，注册了多个pathChildrenCache; 2)无法同步到server重新发布的服务节点。
    }
}
