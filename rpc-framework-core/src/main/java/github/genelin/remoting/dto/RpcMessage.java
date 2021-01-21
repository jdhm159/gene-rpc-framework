package github.genelin.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * rpc调用传输消息类型
 *
 * @author gene lin
 * @createTime 2020/12/7 16:41
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class RpcMessage {

    private Byte serialization;

    private Byte messageType;

    private Object data;

}
