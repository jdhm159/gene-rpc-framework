package github.jdhm159.remoting.constants;

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
public enum RPCResponseCodeEnum {

    SUCCESS(200, "The remote call succeeded"),
    FAIL(500, "The remote call is fail");

    private final int code;
    private final String message;

}
