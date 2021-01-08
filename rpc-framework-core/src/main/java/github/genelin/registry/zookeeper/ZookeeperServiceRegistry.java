package github.genelin.registry.zookeeper;

import github.genelin.common.entity.Holder;
import github.genelin.common.util.factory.SingletonFactory;
import github.genelin.registry.ServiceRegistry;
import github.genelin.registry.zookeeper.listener.ServiceRegistryConnectionListener;
import github.genelin.registry.zookeeper.util.CuratorUtils;
import github.genelin.remoting.constants.RpcConstants;
import github.genelin.remoting.transport.ServiceProvider;
import github.genelin.remoting.transport.ServiceProviderImpl;
import java.net.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

/**
 * @author gene lin
 * @createTime 2020/12/23 14:44
 */
@Slf4j
public class ZookeeperServiceRegistry implements ServiceRegistry {

    private final InetSocketAddress url;
    private Holder<ServiceProvider> providerHolder;

    public ZookeeperServiceRegistry() {
        try {
            this.url = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), RpcConstants.DEFAULT_PORT);
            this.providerHolder = new Holder<>();
        } catch (UnknownHostException e) {
            log.error("Fail to get local host...");
            throw new RuntimeException("Fail to get the local host for server", e);
        }
    }

    @Override
    public void registry(String rpcServiceName) {
        if (!CuratorUtils.isConnecting()) {
            CuratorFramework client = CuratorUtils.getClient();
            client.getConnectionStateListenable().addListener(new ServiceRegistryConnectionListener(getServiceProvider()));
            client.start();
        }
        log.info("Curator[zookeeper] client start successfully...");
        CuratorUtils.createEphemeralNode(rpcServiceName, url);
    }

    private ServiceProvider getServiceProvider(){
        ServiceProvider s = providerHolder.get();
        if (s == null) {
            synchronized (providerHolder) {
                s = providerHolder.get();
                if (s == null) {
                    log.debug("Try to get ServiceProviderImpl instance");
                    providerHolder.set(SingletonFactory.getSingletonObject(ServiceProviderImpl.class));
                    s = providerHolder.get();
                }
            }
        }
        return s;
    }
}
