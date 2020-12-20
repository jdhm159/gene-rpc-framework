package github.jdhm159.remoting.dto;

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
public class RPCResponse {

    private int requestId;

    private int code;     // 响应码

    private int length;     // data内容长度

    private byte[] data;
}
