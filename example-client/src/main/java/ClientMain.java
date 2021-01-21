import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.remoting.transport.RpcClient;
import github.genelin.remoting.transport.netty.client.NettyRpcClient;
import service.HelloService;

/**
 * @author gene lin
 * @createTime 2020/12/22 9:22
 */
public class ClientMain {

    public static void main(String[] args) throws InterruptedException {
        // 1.构建RPC客户端实例（服务消费者）
        RpcClient client = new NettyRpcClient();   // 默认构造方式说明使用了配置文件方式来配置注册中心
        // ZKDiscoveryProperties discoveryProperties = ZKDiscoveryProperties.builder()
        //          .address(127.0.0.1)
        //          .password(admin)
        //          .build();
        // RPCClient client = new NettyClient(discoveryProperties);

        // 2.构建RPC服务标识
        RpcServiceProperties serviceProperties = RpcServiceProperties.builder()
            .interfaceName(HelloService.class.getName())
            .group("demoService")
            .version("1.0")
            .build();

        // 3.获取接口实例（构建动态代理）
        HelloService helloService = client.getInstance(serviceProperties, HelloService.class);
        // HelloService helloService = client.getInstance(HelloService.class);      // 使用默认服务标识(group及version都为"")： 接口名 + "" + ""

        // 4.使用实例

        System.out.println(helloService.hello("gene"));
        System.out.println(helloService.hello("lin"));

    }

}
