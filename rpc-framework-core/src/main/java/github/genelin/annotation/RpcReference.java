package github.genelin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注某个属性为远程服务消费方，将自动进行服务发现并进行属性填充（构造了个代理对象）
 * @author gene lin
 * @createTime 2021/3/3 18:59
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcReference {
    String group() default "";

    String version() default "";
}
