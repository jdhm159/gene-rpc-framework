package github.genelin.common.exception;

import github.genelin.common.enums.RpcErrorMessageEnum;

/**
 * @author gene lin
 * @createTime 2021/1/16 14:54
 */
public class RpcException extends RuntimeException {

    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum){
        super(rpcErrorMessageEnum.getMessage());
    }
}
