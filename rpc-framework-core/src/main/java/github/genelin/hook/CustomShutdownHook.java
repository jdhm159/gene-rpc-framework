package github.genelin.hook;

import github.genelin.common.util.threadpool.ThreadPoolFactoryUtils;
import github.genelin.registry.zookeeper.util.CuratorUtils;
import github.genelin.remoting.transport.netty.client.NettyClientTransport;
import lombok.extern.slf4j.Slf4j;

/**
 * When the server  is closed, do something such as unregister all services or close the eventLoopGroup
 *
 * @author gene lin
 * @createTime 2021/1/19 14:07
 */
@Slf4j
public class CustomShutdownHook {

    public static void addHookForNettyClient(NettyClientTransport nettyClientTransport) {
        log.info("Add shutdownHook for RpcClient to clearAll");
        // close the eventLoopGroup
        Runtime.getRuntime().addShutdownHook(new Thread(nettyClientTransport::close));
    }

    public static void addHookForRpcServer() {
        log.info("Add shutdownHook for RpcServer to clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), NettyRpcServer.PORT);
//                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
//            } catch (UnknownHostException ignored) {
//            }
            CuratorUtils.clearRegistry();
            ThreadPoolFactoryUtils.shutDownAllThreadPool();
        }));
    }

}
