package github.genelin.remoting.transport.netty.server;

import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.common.util.threadpool.ThreadPoolFactoryUtils;
import github.genelin.hook.CustomShutdownHook;
import github.genelin.remoting.constants.RpcConstants;
import github.genelin.remoting.transport.RpcServer;
import github.genelin.remoting.transport.ServiceProvider;
import github.genelin.remoting.transport.ServiceProviderImpl;
import github.genelin.remoting.transport.netty.codec.RpcMessageDecoder;
import github.genelin.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gene lin
 * @createTime 2020/12/21 15:22
 */
@Slf4j
public class NettyRpcServer implements RpcServer {

    public final int port;

    private final ServiceProvider serviceProvider;

    public NettyRpcServer() {
        this(RpcConstants.DEFAULT_PORT);
    }

    public NettyRpcServer(int port) {
        this.port = port;
        this.serviceProvider = SingletonFactory.getSingletonObject(ServiceProviderImpl.class);
        CustomShutdownHook.addHookForRpcServer();
    }

    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        EventExecutorGroup businessEventExecutorGroup = new DefaultEventExecutorGroup(
            NettyRuntime.availableProcessors() * 2,
            ThreadPoolFactoryUtils.createThreadFactory("service-handler-group", false)
        );
        ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                .childOption(ChannelOption.TCP_NODELAY, true)
                // 是否开启 TCP 底层心跳机制
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                .option(ChannelOption.SO_BACKLOG, 128)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        // 30 秒之内没有收到客户端请求的话就关闭连接
                        p.addLast(new IdleStateHandler(30, 0,0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new RpcMessageEncoder());
                        // 使用业务线程池来进行业务处理
                        p.addLast(businessEventExecutorGroup, new NettyRpcServerHandler());
                    }
                });
            // 绑定端口，同步等待绑定成功
            ChannelFuture f = b.bind(port).sync();
            f.addListener((channelFuture) -> {
                if (channelFuture.isSuccess()) {
                    log.info("Server binds successfully...");
                    // 发布服务
                    serviceProvider.publishServices();
                } else {
                    Throwable cause = channelFuture.cause();
                    log.error("Server bound attempt failed", cause);
                    throw new RuntimeException("Server bound attempt failed", cause);
                }
            });
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } catch (Throwable e) {
            log.error("Occur exception when start server:", e);
        } finally {
            log.info("Shutting down the eventLoopGroup and eventExecutorGroup...");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            businessEventExecutorGroup.shutdownGracefully();
        }
    }

    @Override
    public void registerService(RpcServiceProperties serviceProperties, Object serviceImpl) {
        serviceProvider.registerService(serviceProperties, serviceImpl);
    }

    @Override
    public void registerService(Object serviceImpl) {
        serviceProvider.registerService(serviceImpl);
    }

    @Override
    public void registerService(Class<?> serviceInterface, Object serviceImpl) {
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().interfaceName(serviceInterface.getName()).build();
        this.registerService(rpcServiceProperties, serviceImpl);
    }


}
