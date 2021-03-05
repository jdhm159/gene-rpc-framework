package github.genelin.spring;

import github.genelin.annotation.RpcReference;
import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.remoting.transport.netty.client.NettyRpcClient;
import java.lang.reflect.Field;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 实现RpcReference的属性注入，通过beanPostProcessor+反射实现
 * @author gene lin
 * @createTime 2021/3/4 16:45
 */
@Component
@Slf4j
public class RpcReferenceAnnotationBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Field[] declaredFields = bean.getClass().getDeclaredFields();

        for (Field declaredField : declaredFields) {
            RpcReference annotation = declaredField.getAnnotation(RpcReference.class);
            if (annotation != null) {
                Class<?> serviceInterface = declaredField.getType();
                // 构建RPC服务标识
                RpcServiceProperties serviceProperties = RpcServiceProperties.builder()
                    .interfaceName(serviceInterface.getName())
                    .group(annotation.group())
                    .version(annotation.version())
                    .build();
                log.info(serviceProperties.toRPCServiceName());
                NettyRpcClient client = SingletonFactory.getSingletonObject(NettyRpcClient.class);
                // 获取接口实例（构建动态代理）
                Object instance = client.getInstance(serviceProperties, serviceInterface);

                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, instance);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
