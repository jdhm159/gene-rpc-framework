package github.genelin.remoting.constants;

import github.genelin.common.enums.SerializationTypeEnum;

/**
 * @author gene lin
 * @createTime 2020/12/7 9:14
 */
public interface RpcConstants {

    // magic_num
    byte[] MAGIC_CODE = {(byte) 'G', (byte) 'e', (byte) 'n', (byte) 'e'};       // 使用 "Gene" 作为魔数，校验传输格式

    // framework_version
    byte FRAMEWORK_VERSION = 1;

    // the default port that rpc server binds
    int DEFAULT_PORT = 22159;

    int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    // message_type
    byte RPC_REQUEST = (byte) 0x01;
    byte RPC_RESPONSE = (byte) 0x02;
    byte HEARTBEAT_REQUEST = (byte) 0x03;
    byte HEARTBEAT_RESPONSE = (byte) 0x04;

    // serialization
    byte DEFAULT_SERIALIZATION = SerializationTypeEnum.KRYO.getId();

    String PING = "ping";
    String PONG = "pong";

    int DEFAULT_CONNECT_TIMEOUT_MS = 3000;

}
