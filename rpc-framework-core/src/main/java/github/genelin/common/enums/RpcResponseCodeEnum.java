package github.genelin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author gene lin
 * @createTime 2020/12/8 0:31
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {

    SUCCESS(200, "The remote call succeeded"),
    FAIL(500, "The remote call is fail"),
    SERVICE_NOT_FOUND(5001, "Can not find the instance of service for this request"),
    METHOD_NOT_FOUND(5002, "Can not find the method to invoke"),
    CALL_FAILED(5003, "Fail to call the service"),
    SEND_REQUEST_FAIL(5004, "Fail to send rpc request");



    private final int code;
    private final String message;

}
