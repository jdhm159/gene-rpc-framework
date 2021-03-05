package controller;

import github.genelin.annotation.RpcReference;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import service.HelloService;

/**
 * 使用@RpcReference的属性所属类需要是Spring bean，因为使用到了beanPostProcessor实现，且需要通过bean容器来获取这个类的实例
 * @author gene lin
 * @createTime 2021/3/3 18:52
 */
@Component
public class HelloController {

    @RpcReference(group = "demoService", version = "1.0")
    HelloService helloService;

    public void hello(){
        System.out.println(helloService.hello("Gene"));
    }

}
