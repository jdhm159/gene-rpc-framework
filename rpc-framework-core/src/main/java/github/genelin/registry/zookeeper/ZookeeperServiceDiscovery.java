package github.genelin.registry.zookeeper;

import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.common.extension.ExtensionLoader;
import github.genelin.loadbalance.LoadBalance;
import github.genelin.registry.ServiceDiscovery;
import github.genelin.registry.zookeeper.listener.ServiceDiscoveryConnectionListener;
import github.genelin.registry.zookeeper.listener.ServiceRegistryConnectionListener;
import github.genelin.registry.zookeeper.util.CuratorUtils;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

/**
 * @author gene lin
 * @createTime 2020/12/23 14:45
 */
@Slf4j
public class ZookeeperServiceDiscovery implements ServiceDiscovery {

    // key: rpcServiceName       value: URI list
    private final Map<String, List<String>> serviceLists = new ConcurrentHashMap<>();

    /**
     * 客户端默认负载均衡算法实现
     */
    private LoadBalance loadBalance;

    public ZookeeperServiceDiscovery() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getDefaultExtension();
    }

    @Override
    public InetSocketAddress lookupService(RpcServiceProperties rpcServiceProperties) {
        return lookupService(rpcServiceProperties, this.loadBalance);
    }

    /**
     * @param loadBalancer 指定使用的负载均衡算法
     */
    public InetSocketAddress lookupService(RpcServiceProperties rpcServiceProperties, LoadBalance loadBalancer) {
        String rpcServiceName = rpcServiceProperties.toRPCServiceName();
        // 先从缓存获取服务清单
        List<String> urlList = serviceLists.get(rpcServiceName);
        if (urlList == null) {
            log.info("Try to look up service provider from zk server...");
            if (!CuratorUtils.isConnecting()) {
                CuratorFramework client = CuratorUtils.getClient();
                client.getConnectionStateListenable().addListener(new ServiceDiscoveryConnectionListener(serviceLists));
                client.start();
                log.info("Curator[zookeeper] client start successfully...");
            }
            List<String> childNodeList = CuratorUtils.getChildNodeList(rpcServiceName, serviceLists);
            if (childNodeList != null) {
                urlList = childNodeList.parallelStream()
                    .map(nodeName -> new String(CuratorUtils.getNodeData(CuratorUtils.buildPath(rpcServiceName) + "/" + nodeName)))
                    .filter(x -> x.length() != 0).map(x -> x.replace("/", "")).collect(Collectors.toList());
                serviceLists.put(rpcServiceName, urlList);
                return buildUrl(loadBalancer.select(urlList));
            }
        }
        return null;
    }

    private InetSocketAddress buildUrl(String url) {
        String[] ipAndPort = url.split(":");
        try {
            if (ipAndPort.length != 2) {
                throw new RuntimeException("Fail to build URL from String :" + url);
            }
            return new InetSocketAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        } catch (Exception e) {
            log.error("Fail to build URL from String[{}]", url, e);
        }
        throw new RuntimeException("Fail to build URL from String-" + url);
    }

}
