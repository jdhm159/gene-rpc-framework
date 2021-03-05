import github.genelin.annotation.RpcScan;
import github.genelin.remoting.transport.RpcServer;
import github.genelin.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 注解方式发布服务
 * @author gene lin
 * @createTime 2021/2/27 23:35
 */
@RpcScan("service")
public class AnnotationServerMain {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AnnotationServerMain.class);
        applicationContext.start();
        RpcServer server = new NettyRpcServer();
        server.start();
    }
}
