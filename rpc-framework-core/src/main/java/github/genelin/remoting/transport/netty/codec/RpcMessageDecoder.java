package github.genelin.remoting.transport.netty.codec;

import github.genelin.common.enums.SerializationTypeEnum;
import github.genelin.common.extension.ExtensionLoader;
import github.genelin.remoting.constants.RpcConstants;
import github.genelin.remoting.dto.RpcMessage;
import github.genelin.remoting.dto.RpcRequest;
import github.genelin.remoting.dto.RpcResponse;
import github.genelin.serialization.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

/**
 * 继承于 LengthFieldBasedFrameDecoder，基于 length 来划分每个数据帧
 *
 * @author gene lin
 * @createTime 2020/12/21 15:20
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    /**
     * lengthFieldOffset    魔数及其他首部占7bytes（4+1+1+1） lengthFieldLength    length类型为int，所以占4bytes lengthAdjustment
     * length是后面body数据的长度，因此无需要调整 initialBytesToStrip  无需截掉首部内容
     *
     * 提取完整帧（头部+主体数据部分）
     */
    public RpcMessageDecoder() {
        this(RpcConstants.MAX_FRAME_LENGTH, 7, 4, 0, 0);
    }

    private RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
        int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // 借助 LengthFieldBasedFrameDecoder 来进行分离帧
        Object frame = super.decode(ctx, in);
        if (frame != null) {
            try {
                // 解码帧
                return decodeFrame((ByteBuf) frame);
            } catch (Exception e) {
                log.error("Fail to decode from frame", e);
                throw e;
            }
        }
        return null;
    }

    private RpcMessage decodeFrame(ByteBuf buf) {
        int magicCodeLength = RpcConstants.MAGIC_CODE.length;
        byte[] magicCode = new byte[magicCodeLength];
        buf.readBytes(magicCode);
        for (int i = 0; i < magicCodeLength; i++) {
            if (magicCode[i] != RpcConstants.MAGIC_CODE[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(magicCode));
            }
        }
        byte frameworkVersion = buf.readByte();
        if (frameworkVersion != RpcConstants.FRAMEWORK_VERSION) {
            throw new RuntimeException("Incompatible framework version: " + frameworkVersion);
        }

        byte messageType = buf.readByte();
        byte serializationId = buf.readByte();
        int bodyLength = buf.readInt();
        byte[] body = new byte[bodyLength];
        buf.readBytes(body);

        RpcMessage result = RpcMessage.builder()
            .messageType(messageType)
            .serialization(serializationId)
            .build();

        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
            .getExtension(SerializationTypeEnum.getName(serializationId));

        switch (messageType) {
            case RpcConstants.RPC_REQUEST:
                result.setData(serializer.deserialize(body, RpcRequest.class));
                break;
            case RpcConstants.RPC_RESPONSE:
                result.setData(serializer.deserialize(body, RpcResponse.class));
                break;
            default:
                result.setData(serializer.deserialize(body, String.class));
        }

        return result;
    }
}
