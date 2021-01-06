package github.genelin.registry.zookeeper.listener;

import github.genelin.remoting.transport.ServiceProvider;
import java.util.List;
import java.util.Map;
import org.apache.curator.framework.CuratorFramework;

/**
 * @author gene lin
 * @createTime 2020/12/27 10:58
 */
public class ServiceDiscoveryConnectionListener extends AbstractSessionConnectionListener {

    private Map<String, List<String>> serviceLists;

    public ServiceDiscoveryConnectionListener(Map<String, List<String>> serviceLists){
        this.serviceLists = serviceLists;
    }

    @Override
    protected boolean doReconnect(CuratorFramework curatorFramework) {
        return false;
    }
}
