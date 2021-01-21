package github.genelin.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 对于一个服务的标识，由 接口名 interfaceName + group + version 组成
 *
 * @author gene lin
 * @createTime 2020/12/20 16:12
 */
@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class RpcServiceProperties {

    /**
     * 接口名
     */
    private String interfaceName;

    /**
     * 用于区分多个实现
     */
    @Builder.Default()
    private String group = "";

    /**
     * 服务接口版本号，推荐使用 x.x
     */
    @Builder.Default()
    private String version = "";

    public String toRPCServiceName() {
        return getInterfaceName() + getGroup() + getGroup();
    }
}
