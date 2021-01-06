package github.genelin.registry;

import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.extension.SPI;
import java.net.*;

/**
 * @author gene lin
 * @createTime 2020/12/20 14:13
 */
@SPI("zookeeper")
public interface ServiceDiscovery {

    /**
     * 发现服务
     * @param rpcServiceProperties rpc中的服务标识：接口名+group+version
     * @return 一个满足的服务提供方的网络地址（ip+port）
     */
    InetSocketAddress lookupService(RpcServiceProperties rpcServiceProperties);

}