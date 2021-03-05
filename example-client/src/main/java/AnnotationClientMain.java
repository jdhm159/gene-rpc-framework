import controller.HelloController;
import github.genelin.annotation.RpcScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * 注解方式
 * @author gene lin
 * @createTime 2021/3/3 19:19
 */
@RpcScan("controller")
public class AnnotationClientMain {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AnnotationClientMain.class);
        applicationContext.start();
        HelloController helloController = applicationContext.getBean(HelloController.class);
        helloController.hello();
    }
}
