import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.remoting.transport.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import service.HelloService;
import service.HelloServiceImpl1;
import service.HelloServiceImpl2;

/**
 * @author gene lin
 * @createTime 2021/1/7 16:54
 */
@Slf4j
public class ServiceRegistryTest {

    @Test
    public void testLoseConnection() throws InterruptedException {
        // 由ServiceProvider进行获取
        // 需要先构建provider实例，再调用extension方法构建registry
        ServiceProviderImpl provider = SingletonFactory.getSingletonObject(ServiceProviderImpl.class);
        provider.registerService(new HelloServiceImpl1());
        RpcServiceProperties properties = RpcServiceProperties.builder().interfaceName(HelloService.class.getName()).group("impl2").build();
        provider.registerService(properties, new HelloServiceImpl2());
        provider.publishServices();
        Thread.sleep(100 * 1000);

        // 连接到server后，关掉server
        // 会自动对connection尝试重连
        // 60s后，session timeout，发送ConnectionState.Lost事件（只发一次）
        // 开始尝试重连
    }

    @Test
    public void testConnectionReconnect() throws InterruptedException {
        ServiceProviderImpl provider = SingletonFactory.getSingletonObject(ServiceProviderImpl.class);
        provider.registerService(new HelloServiceImpl1());
        RpcServiceProperties properties = RpcServiceProperties.builder().interfaceName(HelloService.class.getName()).group("impl2").build();
        provider.registerService(properties, new HelloServiceImpl2());
        provider.publishServices();
        Thread.sleep(100 * 1000);

        // 预期：连接过期后 按照retrypolicy进行重试，会话过期后（connectionstate.loss）开始重新连接
    }

    @Test
    public void testSessionReconnect() throws InterruptedException {
        ServiceProviderImpl provider = SingletonFactory.getSingletonObject(ServiceProviderImpl.class);
        provider.registerService(new HelloServiceImpl1());
        RpcServiceProperties properties = RpcServiceProperties.builder().interfaceName(HelloService.class.getName()).group("impl2").build();
        provider.registerService(properties, new HelloServiceImpl2());
        provider.publishServices();
        Thread.sleep(60 * 1000);
        System.out.println("Session timeout");
        while (true){}

        // 预期：连接过期后 按照retrypolicy进行重试，会话过期后（connectionstate.loss）开始重新连接，此时恢复server，会话成功重连(此时会使用新的session)，缓存清除
        // 预期外现象：貌似重连后的session还是同一个，导致旧的临时节点没有按照预期随着原来session被清理，使得表现为同一个服务发布了两次。
        // 在最新Apache Curator官方文档中有关于ConnectionState.LOST的描述，大概意思是说：3.x以前版本中ConnectionState.LOST仅表示重试策略失败，不代表会话超时。
        // https://my.oschina.net/u/1241970/blog/918183
        // 处理方案：改变使用Curator版本。升级为4.2.0。不能兼容 3.x版本的zk。
    }

}
