package github.genelin.remoting.transport.netty.client;

import github.genelin.common.entity.Holder;
import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.enums.RpcErrorMessageEnum;
import github.genelin.common.enums.RpcResponseCodeEnum;
import github.genelin.common.exception.RpcException;
import github.genelin.common.extension.ExtensionLoader;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.hook.CustomShutdownHook;
import github.genelin.registry.ServiceDiscovery;
import github.genelin.remoting.constants.RpcConstants;
import github.genelin.remoting.dto.RpcMessage;
import github.genelin.remoting.dto.RpcRequest;
import github.genelin.remoting.dto.RpcResponse;
import github.genelin.remoting.transport.RpcRequestTransport;
import github.genelin.remoting.transport.netty.codec.RpcMessageDecoder;
import github.genelin.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Netty实现相关 提供Rpc请求传输API
 *
 * @author gene lin
 * @createTime 2021/1/17 22:03
 */
@Slf4j
public class NettyClientTransport implements RpcRequestTransport {

    private final EventLoopGroup eventLoopGroup;

    private final Bootstrap bootstrap;

    /**
     * key: rpcServiceName value: Holder of Channel
     */
    private final ConcurrentHashMap<String, Holder<Channel>> channelHolders;

    private final UnprocessedRequests unprocessedRequests;

    private final ServiceDiscovery serviceDiscovery;

    public NettyClientTransport() {
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(eventLoopGroup)
            .channel(NioSocketChannel.class)
            // 启用 TCP keepAlive
            .option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    // 每5s发送一次心跳包
                    p.addLast(new IdleStateHandler(60, 6, 0, TimeUnit.SECONDS));
                    p.addLast(new RpcMessageEncoder());
                    p.addLast(new RpcMessageDecoder());
                    p.addLast(new NettyRpcClientHandler());
                }
            });
        this.channelHolders = new ConcurrentHashMap<>();
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getDefaultExtension();
        this.unprocessedRequests = SingletonFactory.getSingletonObject(UnprocessedRequests.class);
        CustomShutdownHook.addHookForNettyClient(this);
    }

    /**
     * 向服务提供方发送Rpc请求并获得Rpc响应
     *
     * @param rpcRequest Rpc请求
     * @return Rpc响应
     */
    public RpcResponse<Object> sendRpcRequest(RpcRequest rpcRequest) {
        RpcServiceProperties properties = rpcRequest.toRpcServiceProperties();
        String requestId = rpcRequest.getRequestId();
        String key = properties.toRPCServiceName();

        final Holder<Channel> holder = getHolderOrCreate(key);
        Channel channel = getChannelOrCreate(holder, properties);

        CompletableFuture<RpcResponse<Object>> cf = new CompletableFuture<>();
        unprocessedRequests.put(String.valueOf(requestId), cf);
        RpcMessage rpcMessage = RpcMessage.builder().messageType(RpcConstants.RPC_REQUEST)
            .serialization(RpcConstants.DEFAULT_SERIALIZATION)
            .data(rpcRequest)
            .build();

        try {
            log.info("Try to send rpcRequest on channel[{}]...", channel);
            channel.writeAndFlush(rpcMessage).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (!future.isSuccess()) {
                        // 写不成功，直接将通道关闭，holder也set null，等待下次请求再重新构建通道
                        future.channel().close();
                        throw new RuntimeException("Fail to write rpcRequest!", future.cause());
                    }
                }
            });

        } catch (Exception e) {
            log.error("Fail to write RpcMessage...", e);
            unprocessedRequests.remove(requestId);
            return RpcResponse.fail(RpcResponseCodeEnum.SEND_REQUEST_FAIL, requestId);
        }

        // 成功写入请求
        try {
            // 同步阻塞获取rpcResponse
            return cf.get();
        } catch (InterruptedException | ExecutionException e) {
            return RpcResponse.fail(RpcResponseCodeEnum.FAIL, requestId);
        }
    }

    public void close() {
        eventLoopGroup.shutdownGracefully();
    }

    private Holder<Channel> getHolderOrCreate(String rpcServiceName) {
        Holder<Channel> holder = channelHolders.get(rpcServiceName);
        if (holder == null) {
            channelHolders.putIfAbsent(rpcServiceName, new Holder<>());
            holder = channelHolders.get(rpcServiceName);
        }
        return holder;
    }

    private Channel getChannelOrCreate(Holder<Channel> holder, RpcServiceProperties properties) {
        Channel channel = holder.get();
        if (channel == null) {
            synchronized (holder) {
                channel = holder.get();
                if (channel == null) {
                    holder.set(buildChannel(properties));
                    channel = holder.get();
                }
            }
        }
        return channel;
    }

    private Channel buildChannel(RpcServiceProperties rpcServiceProperties) {
        // 服务发现
        InetSocketAddress serviceProvider = serviceDiscovery.lookupService(rpcServiceProperties);
        if (serviceProvider == null) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_PROVIDER_NOT_FOUND);
        }

        log.info("Try to connect to service provider[{}]", serviceProvider);
        ChannelFuture cf = bootstrap.connect(serviceProvider.getAddress(), serviceProvider.getPort());
        // 限时阻塞同步获得channel
        boolean connected = cf.awaitUninterruptibly(RpcConstants.DEFAULT_CONNECT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (connected && cf.isSuccess()) {
            Channel channel = cf.channel();
            channel.closeFuture().addListener(future -> {
                log.info("Old channel[{}] has been closed", channel);
                Holder<Channel> holder = channelHolders.get(rpcServiceProperties.toRPCServiceName());
                if (channel.equals(holder.get())) {
                    holder.set(null);
                }
            });
            log.info("Success to build channel for the RpcService[{}]", rpcServiceProperties);
            return channel;
        }
        throw new RpcException("Fail to connect to remote service provider", cf.cause());
    }
}
