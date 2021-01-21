package github.genelin.remoting.transport.netty.client;

import github.genelin.common.entity.Holder;
import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.extension.ExtensionLoader;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.proxy.RpcClientProxy;
import github.genelin.registry.ServiceDiscovery;
import github.genelin.remoting.dto.RpcRequest;
import github.genelin.remoting.dto.RpcResponse;
import github.genelin.remoting.transport.RpcClient;
import github.genelin.remoting.transport.RpcRequestTransport;
import github.genelin.remoting.transport.netty.codec.RpcMessageDecoder;
import github.genelin.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gene lin
 * @createTime 2020/12/21 15:22
 */
@Slf4j
public class NettyRpcClient implements RpcClient {

    private final RpcRequestTransport nettyClientTransport;

    public NettyRpcClient() {
        nettyClientTransport = SingletonFactory.getSingletonObject(NettyClientTransport.class);
    }

    @Override
    public <T> T getInstance(RpcServiceProperties serviceProperties, Class<T> serviceInterface) {
        return new RpcClientProxy(nettyClientTransport, serviceProperties).getInstance(serviceInterface);
    }

    @Override
    public <T> T getInstance(Class<T> serviceInterface) {
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
            .version("")
            .group("")
            .interfaceName(serviceInterface.getName())
            .build();
        return getInstance(rpcServiceProperties, serviceInterface);
    }
}
