package github.genelin.proxy;

import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.enums.RpcErrorMessageEnum;
import github.genelin.common.enums.RpcResponseCodeEnum;
import github.genelin.common.exception.RpcException;
import github.genelin.remoting.dto.RpcRequest;
import github.genelin.remoting.dto.RpcResponse;
import github.genelin.remoting.transport.RpcRequestTransport;
import github.genelin.remoting.transport.netty.client.NettyClientTransport;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态代理 逻辑实现 1）构造Rpc请求并调用发送
 *
 * @author gene lin
 * @createTime 2021/1/17 10:44
 */
public class RpcClientProxy implements InvocationHandler {

    private final RpcRequestTransport nettyClientTransport;

    private final RpcServiceProperties rpcServiceProperties;

    public RpcClientProxy(RpcRequestTransport nettyClientTransport, RpcServiceProperties rpcServiceProperties) {
        this.nettyClientTransport = nettyClientTransport;
        this.rpcServiceProperties = rpcServiceProperties;
    }

    public <T> T getInstance(Class<T> interfaceToImpl) {
        ClassLoader classLoader = interfaceToImpl.getClassLoader();
        Object instance = Proxy.newProxyInstance(classLoader, new Class[] {interfaceToImpl}, this);
        return interfaceToImpl.cast(instance);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // build rpcRequest instance
        RpcRequest rpcRequest = RpcRequest.builder()
            .group(rpcServiceProperties.getGroup())
            .version(rpcServiceProperties.getVersion())
            .requestId(UUID.randomUUID().toString())
            .interfaceName(rpcServiceProperties.getInterfaceName())
            .methodName(method.getName())
            .paramsValue(args)
            .paramsTypes(method.getParameterTypes())
            .build();
        RpcResponse<Object> rpcResponse = nettyClientTransport.sendRpcRequest(rpcRequest);
        checkResponse(rpcResponse);
        return rpcResponse.getData();
    }

    private void checkResponse(RpcResponse<Object> rpcResponse) {
        if (rpcResponse.getCode() == null || !rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())) {
//            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ":" + rpcRequest.getInterfaceName());
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE);
        }
    }


}
