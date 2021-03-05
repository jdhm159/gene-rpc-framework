import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.remoting.transport.RpcServer;
import github.genelin.remoting.transport.netty.server.NettyRpcServer;
import service.HelloService;
import service.HelloServiceImpl;

/**
 * 普通方式发布服务
 * @author gene lin
 * @createTime 2020/12/22 9:38
 */
public class ServerMain {

    public static void main(String[] args) {
        // 1.创建RPC服务器实例（服务提供方）
        RpcServer server = new NettyRpcServer();   // 默认构造方式说明使用了配置文件方式来配置注册中心
        // ZKRegistryProperties registryProperties = ZKServiceRegistry.builder()
        //          .address(127.0.0.1)
        //          .password(admin)
        //          .build();
        // RPCServer server = new NettyServer(registryProperties);

        // 2.实例化接口实现类
        HelloService serviceImpl = new HelloServiceImpl();
        // 3.构建服务标识
        RpcServiceProperties serviceProperties = RpcServiceProperties.builder()
            .interfaceName(HelloService.class.getName())
            .group("demoService")
            .version("1.0")
            .build();
        // 4.服务注册（注册到本地ServiceProvider）
        server.registerService(serviceProperties, serviceImpl);
        // 如果不造成冲突的话，构建服务标识步骤可以省略掉
        // server.registerService(HelloService.class, serviceImpl);

        // 5.开始提供服务(接收请求进行响应)
        server.start();
    }

}
