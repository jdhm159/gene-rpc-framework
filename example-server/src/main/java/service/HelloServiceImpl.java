package service;

import github.genelin.annotation.RpcService;
import java.lang.annotation.Target;

/**
 * @author gene lin
 * @createTime 2020/12/22 9:30
 */
@RpcService(group = "demoService", version = "1.0")
public class HelloServiceImpl implements HelloService {

    public String hello(String name) {
        return "Hello " + name;
    }
}
