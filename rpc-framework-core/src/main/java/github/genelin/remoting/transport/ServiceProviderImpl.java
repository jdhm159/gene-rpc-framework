package github.genelin.remoting.transport;

import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.extension.ExtensionLoader;
import github.genelin.registry.ServiceRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * 管理服务实例 同时负责发布服务
 *
 * 单例，通过单例工厂获得实例
 *
 * @author gene lin
 * @createTime 2020/12/27 16:07
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    private final Map<String, Object> servicesInstances = new HashMap<>();
    private final ServiceRegistry serviceRegistry;

    // 私有化构造方法，保证单例
    private ServiceProviderImpl() {
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getDefaultExtension();
    }

    @Override
    public void registerService(RpcServiceProperties properties, Object serviceImpl) {
        servicesInstances.put(properties.toRPCServiceName(), serviceImpl);
    }

    @Override
    public void registerService(Object serviceImpl) {
        RpcServiceProperties defaultRPCService = RpcServiceProperties.builder()
            .interfaceName(serviceImpl.getClass().getInterfaces()[0].getName())
            .group("")
            .version("")
            .build();
        registerService(defaultRPCService, serviceImpl);
    }

    @Override
    public boolean contains(RpcServiceProperties properties) {
        return servicesInstances.containsKey(properties.toRPCServiceName());
    }


    @Override
    public void publishServices() {
        if (servicesInstances.size() == 0){
            log.info("Has no service to publish");
            return;
        }
        for (String rpcServiceName : servicesInstances.keySet()) {
            try {
                serviceRegistry.registry(rpcServiceName);
                log.info("Service[{}] publish success", rpcServiceName);
            } catch (Exception e) {
                log.error("Fail to publish Service[{}]", rpcServiceName, e);
            }
        }
        log.info("All Service publish success");
    }

    @Override
    public Object getServiceInstance(String rpcServiceName) {
        return servicesInstances.get(rpcServiceName);
    }
}
