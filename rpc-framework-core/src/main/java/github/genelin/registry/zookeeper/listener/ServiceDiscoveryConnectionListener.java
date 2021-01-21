package github.genelin.registry.zookeeper.listener;

import github.genelin.registry.zookeeper.util.CuratorUtils;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

/**
 * @author gene lin
 * @createTime 2020/12/27 10:58
 */
@Slf4j
public class ServiceDiscoveryConnectionListener extends AbstractSessionConnectionListener {

    private Map<String, List<String>> serviceLists;

    public ServiceDiscoveryConnectionListener(Map<String, List<String>> serviceLists) {
        this.serviceLists = serviceLists;
    }

    @Override
    protected void afterSessionTimeout(CuratorFramework curatorFramework) {
        serviceLists.clear();
        CuratorUtils.clearDiscoveryCache();
        log.info("After session timeout: finish clearing cache");
    }
}
