package github.jdhm159.remoting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author gene lin
 * @createTime 2020/12/6 23:16
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class RPCRequest {

    private String interfaceName;

    private String methodName;

    private Class<?>[] paramsTypes;

    private Object[] paramsValue;

    private int requestId;

    private String group;

    // the service interface version is different from the framework version
    private String version;
}
