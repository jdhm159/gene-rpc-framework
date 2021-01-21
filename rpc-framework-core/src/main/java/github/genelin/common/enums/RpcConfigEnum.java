package github.genelin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author gene lin
 * @createTime 2020/12/25 16:03
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {
    RPC_CONFIG_FILENAME("rpc.properties"),
    ZOOKEEPER_SERVER_URL("rpc.zookeeper.address"),
    ZOOKEEPER_DIGEST_USERNAME("rpc.zookeeper.digest.username"),
    ZOOKEEPER_DIGEST_PASSWORD("rpc.zookeeper.digest.password"),
    ZOOKEEPER_ROOT_PATH("rpc.zookeeper.root.path");

    private String propertyName;
}
