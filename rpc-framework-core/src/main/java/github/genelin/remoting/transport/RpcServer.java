package github.genelin.remoting.transport;

import github.genelin.common.entity.RpcServiceProperties;

/**
 * 负责管理服务实例，绑定监听端口以接受RPC请求并进行响应
 * 门面模式，暴露api
 *
 * @author gene lin
 * @createTime 2020/12/20 14:10
 */
public interface RpcServer {

    /**
     * 配置已完毕，开始提供服务
     */
    void start();

    /**
     * 将要暴露的服务注册到Server（此时未真正将服务暴露）
     * @param serviceProperties
     * @param serviceImpl
     */
    void registerService(RpcServiceProperties serviceProperties, Object serviceImpl);

    void registerService(Object serviceImpl);

    void registerService(Class<?> serviceInterface, Object serviceImpl);

}
