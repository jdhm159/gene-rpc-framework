package github.genelin.registry.zookeeper.listener;

import github.genelin.registry.zookeeper.util.CuratorUtils;
import github.genelin.remoting.transport.ServiceProvider;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

/**
 *
 * @author gene lin
 * @createTime 2020/12/27 10:59
 */
@Slf4j
public class ServiceRegistryConnectionListener extends AbstractSessionConnectionListener {

    private ServiceProvider serviceProvider;
    public ServiceRegistryConnectionListener(ServiceProvider serviceProvider){
        this.serviceProvider = serviceProvider;
    }
    @Override
    protected void afterReconnected(CuratorFramework curatorFramework) {
        // 重新发布服务，写入临时节点
        serviceProvider.publishServices();
        log.info("After service registry reconnect: republic the service to zk server");
    }
}
