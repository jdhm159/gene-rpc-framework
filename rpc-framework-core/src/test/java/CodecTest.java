import github.genelin.common.enums.SerializationTypeEnum;
import github.genelin.common.extension.ExtensionLoader;
import github.genelin.remoting.constants.RpcConstants;
import github.genelin.remoting.dto.RpcMessage;
import github.genelin.remoting.dto.RpcRequest;
import github.genelin.remoting.transport.netty.codec.RpcMessageDecoder;
import github.genelin.remoting.transport.netty.codec.RpcMessageEncoder;
import github.genelin.serialization.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import service.HelloService;

/**
 * @author gene lin
 * @createTime 2021/1/13 21:34
 */
public class CodecTest {

    @Test
    public void testEncoder() {
        byte messageType = RpcConstants.RPC_REQUEST;
        byte serialization = SerializationTypeEnum.KRYO.getId();
        RpcMessage rpcMessage = RpcMessage.builder()
            .messageType(messageType)
            .serialization(serialization)
            .build();
        RpcRequest rpcRequest = RpcRequest.builder()
            .interfaceName(HelloService.class.getName())
            .methodName("hello")
            .paramsTypes(null)
            .paramsValue(null)
            .build();
        rpcMessage.setData(rpcRequest);

        EmbeddedChannel channel = new EmbeddedChannel(new RpcMessageEncoder());
        Assert.assertTrue(channel.writeOutbound(rpcMessage));
        ByteBuf buf = (ByteBuf) channel.readOutbound();

        byte[] readMagicCode = new byte[4];
        buf.readBytes(readMagicCode);
        Assert.assertArrayEquals(RpcConstants.MAGIC_CODE, readMagicCode);

        Assert.assertEquals(RpcConstants.FRAMEWORK_VERSION, buf.readByte());
        Assert.assertEquals(messageType, buf.readByte());
        Assert.assertEquals(serialization, buf.readByte());

        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
            .getExtension(SerializationTypeEnum.getName(serialization));
        byte[] serialized = serializer.serialize(rpcRequest);
        Assert.assertEquals(serialized.length, buf.readInt());
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        Assert.assertArrayEquals(serialized, data);
        Assert.assertFalse(buf.isReadable());

        Assert.assertFalse(channel.finish());
        Assert.assertNull(channel.readInbound());

    }

    @Test
    public void testDecoder() {
        byte messageType = RpcConstants.RPC_REQUEST;
        byte serialization = SerializationTypeEnum.KRYO.getId();
        RpcMessage rpcMessage = RpcMessage.builder()
            .messageType(messageType)
            .serialization(serialization)
            .build();
        RpcRequest rpcRequest = RpcRequest.builder()
            .interfaceName(HelloService.class.getName())
            .methodName("hello")
            .paramsTypes(null)
            .paramsValue(null)
            .build();
        rpcMessage.setData(rpcRequest);

        EmbeddedChannel channel1 = new EmbeddedChannel(new RpcMessageEncoder());
        channel1.writeOutbound(rpcMessage);
        ByteBuf buf = (ByteBuf) channel1.readOutbound();

        EmbeddedChannel channel2 = new EmbeddedChannel(new RpcMessageDecoder());
        Assert.assertTrue(channel2.writeInbound(buf));
        RpcMessage decoded = (RpcMessage) channel2.readInbound();

        Assert.assertEquals(messageType, decoded.getMessageType());
        Assert.assertEquals(serialization, decoded.getSerialization());
        RpcRequest decodedRequest = (RpcRequest) decoded.getData();
        Assert.assertEquals(rpcRequest.getInterfaceName(), decodedRequest.getInterfaceName());
        Assert.assertEquals(rpcRequest.getMethodName(), decodedRequest.getMethodName());
        Assert.assertArrayEquals(rpcRequest.getParamsTypes(), decodedRequest.getParamsTypes());
        Assert.assertArrayEquals(rpcRequest.getParamsValue(), decodedRequest.getParamsValue());
    }
}
