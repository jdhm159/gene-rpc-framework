package github.genelin.remoting.handler;

import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.enums.RpcResponseCodeEnum;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.remoting.dto.RpcRequest;
import github.genelin.remoting.dto.RpcResponse;
import github.genelin.remoting.transport.ServiceProvider;
import github.genelin.remoting.transport.ServiceProviderImpl;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * rpc请求处理器，获得服务实例并调用对应方法
 * @author gene lin
 * @createTime 2021/1/15 14:43
 */
@Slf4j
public class RpcRequestHandler {

    private ServiceProvider serviceProvider;

    public RpcRequestHandler() {
        this.serviceProvider = SingletonFactory.getSingletonObject(ServiceProviderImpl.class);
    }

    public RpcResponse handlerRpcRequest(RpcRequest rpcRequest) {
        RpcServiceProperties rpcServiceProperties = rpcRequest.toRpcServiceProperties();
        Object serviceInstance = serviceProvider.getServiceInstance(rpcServiceProperties);
        if (serviceInstance == null) {
            log.error("Request handling failure: Can not find the instance of the service[{}]", rpcServiceProperties.toRPCServiceName());
            return RpcResponse.fail(RpcResponseCodeEnum.SERVICE_NOT_FOUND, rpcRequest.getRequestId());
        }
        return invokeTargetMethod(rpcRequest, serviceInstance);
    }

    private RpcResponse invokeTargetMethod(RpcRequest request, Object serviceInstance) {
        String requestId = request.getRequestId();
        try {
            Class<?> instanceClass = serviceInstance.getClass();
            Method method = instanceClass.getMethod(request.getMethodName(), request.getParamsTypes());
            Class<?> returnType = method.getReturnType();
            Object result = returnType.cast(method.invoke(serviceInstance, request.getParamsValue()));
            return RpcResponse.success(result, requestId);
        } catch (NoSuchMethodException e) {
            log.error("Request handling failure: Can not find the method to invoke", e);
            return RpcResponse.fail(RpcResponseCodeEnum.METHOD_NOT_FOUND, requestId);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Request handling failure: Can not invoke the method", e);
            return RpcResponse.fail(RpcResponseCodeEnum.CALL_FAILED, requestId);
        }
    }
}
