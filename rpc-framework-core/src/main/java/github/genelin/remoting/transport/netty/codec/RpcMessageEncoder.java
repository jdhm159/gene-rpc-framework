package github.genelin.remoting.transport.netty.codec;

import github.genelin.common.enums.SerializationTypeEnum;
import github.genelin.common.extension.ExtensionLoader;
import github.genelin.remoting.constants.RpcConstants;
import github.genelin.remoting.dto.RpcMessage;
import github.genelin.serialization.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 0                     4                 5              6                7                 11
 * +---------------------+-----------------+--------------+----------------+-----------------+
 * |     Magic_code      |Framework_version| Message_type |Serialization_id|    Body_length  |
 * +-----------------------------------------------------------------------------------------+
 * |                                       ...                                               |
 * |                                   body(serialized)                                      |
 * |                                        ...                                               |
 * +-----------------------------------------------------------------------------------------+
 *
 * To-Do：1）对于心跳检测包的特殊处理；2）压缩算法
 * @author gene lin
 * @createTime 2020/12/21 15:15
 */
@Slf4j
@Sharable
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        try {

            out.writeBytes(RpcConstants.MAGIC_CODE);
            out.writeByte(RpcConstants.FRAMEWORK_VERSION);
            out.writeByte(msg.getMessageType());
            byte serializationId = msg.getSerialization();
            out.writeByte(serializationId);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
                .getExtension(SerializationTypeEnum.getName(serializationId));
            byte[] serializedData = serializer.serialize(msg.getData());
            out.writeInt(serializedData.length);
            out.writeBytes(serializedData);

        } catch (Exception e) {
            log.error("Fail to encode message", e);
        }
    }
}
