package github.jdhm159.remoting.constants;

import lombok.AllArgsConstructor;

/**
 * @author gene lin
 * @createTime 2020/12/8 0:22
 */
@AllArgsConstructor
public enum RPCMessageTypeEnum {

    RPC_REQUEST((byte) 0x01),
    RPC_RESPONSE((byte) 0x02),
    HEARTBEAT_REQUEST((byte) 0x03),
    HEARTBEAT_RESPONSE((byte) 0x04);

    private final byte id;
}
