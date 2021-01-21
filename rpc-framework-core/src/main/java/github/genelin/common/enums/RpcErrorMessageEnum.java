package github.genelin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author gene lin
 * @createTime 2021/1/17 23:48
 */
@AllArgsConstructor
@Getter
public enum  RpcErrorMessageEnum {

    SERVICE_PROVIDER_NOT_FOUND("Found no service provider"),
    SERVICE_INVOCATION_FAILURE("服务调用失败");

    private final String message;
}
