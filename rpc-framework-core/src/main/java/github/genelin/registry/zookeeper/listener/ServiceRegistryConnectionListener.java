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
    protected boolean doReconnect(CuratorFramework curatorFramework) {
        boolean result = false;
        try {
            result = curatorFramework.getZookeeperClient().blockUntilConnectedOrTimedOut();
            if (result){
                // 重新发布服务，写入临时节点
                serviceProvider.publishService();
                log.info("Success to reconnect to zk server");
            }else {
                log.info("Fail to reconnect to zk server");
            }
        } catch (InterruptedException e) {
            log.error("Fail to reconnect to server...", e);
        }
        return result;
    }
}
