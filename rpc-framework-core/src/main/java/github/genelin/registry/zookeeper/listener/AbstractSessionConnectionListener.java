package github.genelin.registry.zookeeper.listener;

import github.genelin.registry.zookeeper.util.CuratorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * 监听连接状态
 *
 * @author gene lin
 * @createTime 2020/12/27 10:23
 */
@Slf4j
public abstract class AbstractSessionConnectionListener implements ConnectionStateListener {

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (ConnectionState.LOST.equals(newState)) {
            log.error("zk连接异常：Zookeeper session超时");
            afterSessionTimeout(client);
            while (true) {
                log.info("尝试重新连接到zk server...");
                boolean reconnected = false;
                try {
                    reconnected = client.getZookeeperClient().blockUntilConnectedOrTimedOut();
                    if (reconnected) {
                        log.info("Success to reconnect to zk server");
                        afterReconnected(client);
                        break;
                    } else {
                        log.info("Fail to reconnect to zk server");
                    }
                } catch (InterruptedException e) {
                    log.error("Fail to reconnect to server...", e);
                }
            }
            log.info("zk重连：成功重新连接到 zk server ...");
        }

    }

    protected void afterReconnected(CuratorFramework curatorFramework){};

    protected void afterSessionTimeout(CuratorFramework curatorFramework){};
}
