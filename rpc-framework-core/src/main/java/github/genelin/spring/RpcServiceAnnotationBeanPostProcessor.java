package github.genelin.spring;

import github.genelin.annotation.RpcService;
import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.remoting.transport.RpcServer;
import github.genelin.remoting.transport.ServiceProvider;
import github.genelin.remoting.transport.ServiceProviderImpl;
import github.genelin.remoting.transport.netty.server.NettyRpcServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author gene lin
 * @createTime 2021/3/4 20:49
 */
@Component
public class RpcServiceAnnotationBeanPostProcessor implements BeanPostProcessor {

    private ServiceProvider serviceProvider;

    public RpcServiceAnnotationBeanPostProcessor(){
        serviceProvider = SingletonFactory.getSingletonObject(ServiceProviderImpl.class);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        RpcService annotation = bean.getClass().getAnnotation(RpcService.class);
        if (annotation != null){
            Class<?> serviceInterface = bean.getClass().getInterfaces()[0];
            RpcServiceProperties serviceProperties = RpcServiceProperties.builder()
                .interfaceName(serviceInterface.getName())
                .group(annotation.group())
                .version(annotation.version())
                .build();
            serviceProvider.registerService(serviceProperties, bean);
        }
        return bean;
    }
}
