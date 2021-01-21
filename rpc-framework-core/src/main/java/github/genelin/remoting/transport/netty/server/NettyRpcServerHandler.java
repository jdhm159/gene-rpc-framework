package github.genelin.remoting.transport.netty.server;

import github.genelin.remoting.constants.RpcConstants;
import github.genelin.remoting.dto.RpcMessage;
import github.genelin.remoting.dto.RpcRequest;
import github.genelin.remoting.dto.RpcResponse;
import github.genelin.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务端处理器
 * 负责 接收rpc请求并进行响应、心跳检测
 * @author gene lin
 * @createTime 2021/1/14 15:52
 */
@Slf4j
@Sharable
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private RpcRequestHandler requestHandler;

    public NettyRpcServerHandler() {
        requestHandler = new RpcRequestHandler();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        RpcMessage rpcMessage = RpcMessage.builder().serialization(msg.getSerialization()).build();
        rpcMessage.setSerialization(RpcConstants.DEFAULT_SERIALIZATION);
        byte messageType = msg.getMessageType();
        if (messageType == RpcConstants.RPC_REQUEST) {
            try {
                RpcRequest rpcRequest = (RpcRequest) msg.getData();
                log.info("Received a rpc request: {}", rpcRequest);
                RpcResponse response = requestHandler.handlerRpcRequest(rpcRequest);
                log.debug("Request handle success: ");
                rpcMessage.setMessageType(RpcConstants.RPC_RESPONSE);
                rpcMessage.setData(response);
            } catch (Exception e) {
                log.error("Fail to handle the request!", e);
                return;
            }
        } else {
            if (messageType == RpcConstants.HEARTBEAT_REQUEST) {
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE);
                rpcMessage.setData(RpcConstants.PONG);
            }
        }
        ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("Idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Netty server catch exception", cause);
        ctx.close();
    }
}
