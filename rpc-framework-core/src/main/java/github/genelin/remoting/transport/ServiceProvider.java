package github.genelin.remoting.transport;

import github.genelin.common.entity.RpcServiceProperties;
import java.util.List;
import java.util.Set;

/**
 * 服务提供者（负责保存管理服务实例）
 * @author gene lin
 * @createTime 2020/12/21 14:13
 */
public interface ServiceProvider {

    void registerService(RpcServiceProperties properties, Object serviceImpl);

    void registerService(Object serviceImpl);

    boolean contains(RpcServiceProperties properties);

    void publishServices();

    Object getServiceInstance(RpcServiceProperties properties);

}
