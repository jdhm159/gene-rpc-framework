package github.genelin.registry.zookeeper;

import github.genelin.registry.ServiceRegistry;
import github.genelin.registry.zookeeper.listener.ServiceRegistryConnectionListener;
import github.genelin.registry.zookeeper.util.CuratorUtils;
import github.genelin.remoting.constants.RpcConstants;
import github.genelin.remoting.transport.ServiceProvider;
import java.net.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

/**
 * @author gene lin
 * @createTime 2020/12/23 14:44
 */
@Slf4j
public class ZookeeperServiceRegistry implements ServiceRegistry {

    private InetSocketAddress url;
    private ServiceProvider serviceProvider;

    public ZookeeperServiceRegistry(ServiceProvider serviceProvider) {
        try {
            this.serviceProvider = serviceProvider;
            this.url = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), RpcConstants.DEFAULT_PORT);
        } catch (UnknownHostException e) {
            log.error("Fail to get local host...");
            throw new RuntimeException("Fail to get the local host for server", e);
        }
    }

    @Override
    public void registry(String serviceProperties) {
        if (!CuratorUtils.isConnecting()){
            CuratorFramework client = CuratorUtils.getClient();
            client.getConnectionStateListenable().addListener(new ServiceRegistryConnectionListener(serviceProvider));
            client.start();
        }
        log.info("Curator[zookeeper] client start successfully...");
        CuratorUtils.createEphemeralNode(serviceProperties, url);
    }

}
