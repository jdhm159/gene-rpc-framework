package service;

/**
 * @author gene lin
 * @createTime 2020/12/22 9:30
 */
public class HelloServiceImpl implements HelloService{

    public String hello(String name) {
        return "Hello " + name;
    }
}
