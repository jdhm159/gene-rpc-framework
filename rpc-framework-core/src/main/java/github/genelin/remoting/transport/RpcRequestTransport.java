package github.genelin.remoting.transport;

import github.genelin.remoting.dto.RpcRequest;
import github.genelin.remoting.dto.RpcResponse;

/**
 * @author gene lin
 * @createTime 2021/1/18 0:48
 */
public interface RpcRequestTransport {
    RpcResponse<Object> sendRpcRequest(RpcRequest rpcRequest);
}
