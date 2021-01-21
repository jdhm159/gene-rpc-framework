package github.genelin.remoting.transport.netty.client;

import github.genelin.common.enums.SerializationTypeEnum;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.remoting.constants.RpcConstants;
import github.genelin.remoting.dto.RpcMessage;
import github.genelin.remoting.dto.RpcMessage.RpcMessageBuilder;
import github.genelin.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.net.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端处理器 负责 接收rpc响应并进行处理、心跳检测
 *
 * @author gene lin
 * @createTime 2021/1/15 22:32
 */
@Slf4j
@Sharable
public class NettyRpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {

    private final UnprocessedRequests unprocessedRequests = SingletonFactory.getSingletonObject(UnprocessedRequests.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
        byte messageType = msg.getMessageType();
        if (messageType == RpcConstants.RPC_RESPONSE) {
            unprocessedRequests.complete((RpcResponse<Object>) msg.getData());
        } else if (messageType == RpcConstants.HEARTBEAT_RESPONSE) {
            log.info("Receiving heartbeat response[pong]");
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("Write idle happen [{}], send heartbeat request[ping]", ctx.channel().remoteAddress());
                RpcMessage heartbeatRequest = RpcMessage.builder()
                    .serialization(RpcConstants.DEFAULT_SERIALIZATION)
                    .messageType(RpcConstants.HEARTBEAT_REQUEST)
                    .data(RpcConstants.PING).build();
                ctx.writeAndFlush(heartbeatRequest).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else if (state == IdleState.READER_IDLE) {
                log.info("Idle check happen, so close the connection");
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Netty client catch exception", cause);
        ctx.close();
    }

}
