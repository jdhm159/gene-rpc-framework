package github.genelin.remoting.dto;

import github.genelin.common.enums.RpcResponseCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author gene lin
 * @createTime 2020/12/6 23:17
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class RpcResponse<T> {

    private String requestId;

    private Integer code;     // 响应码

    private String message;

    private T data;

    public static <R> RpcResponse<R> success(R data, String requestId){
        RpcResponse<R> result = new RpcResponse<>();
        result.setCode(RpcResponseCodeEnum.SUCCESS.getCode());
        result.setMessage(RpcResponseCodeEnum.SUCCESS.getMessage());
        result.setRequestId(requestId);
        if (data != null){
            result.setData(data);
        }
        return result;
    }

    public static RpcResponse<Object> fail(RpcResponseCodeEnum responseCodeEnum, String requestId){
        return (RpcResponse<Object>) RpcResponse.builder()
            .code(responseCodeEnum.getCode())
            .message(responseCodeEnum.getMessage())
            .requestId(requestId)
            .build();
    }
}
