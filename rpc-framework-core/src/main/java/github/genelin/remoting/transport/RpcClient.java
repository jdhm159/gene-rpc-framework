package github.genelin.remoting.transport;

import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.remoting.dto.RpcRequest;

/**
 * 门面模式，暴露 api
 *
 * @author gene lin
 * @createTime 2020/12/20 14:09
 */
public interface RpcClient {

    <T> T getInstance(RpcServiceProperties serviceProperties, Class<T> serviceInterface);

    <T> T getInstance(Class<T> serviceInterface);
}
