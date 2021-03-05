package github.genelin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注某个类为rpc服务提供方实现，将自动对改类进行实例化并进行服务发布
 * @author gene lin
 * @createTime 2021/2/27 23:15
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcService {
    String group() default "";
    String version() default "";
}
