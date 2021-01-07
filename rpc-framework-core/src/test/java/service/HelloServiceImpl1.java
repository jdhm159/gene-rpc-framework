package service;

/**
 * @author gene lin
 * @createTime 2021/1/7 16:24
 */

public class HelloServiceImpl1 implements HelloService {

    @Override
    public String hello() {
        return "This is impl1";
    }
}