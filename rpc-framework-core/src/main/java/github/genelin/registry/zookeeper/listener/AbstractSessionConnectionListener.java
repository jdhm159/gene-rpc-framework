package github.genelin.registry.zookeeper.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * 监听连接状态
 * @author gene lin
 * @createTime 2020/12/27 10:23
 */
@Slf4j
public abstract class AbstractSessionConnectionListener implements ConnectionStateListener {

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if (ConnectionState.LOST.equals(newState)) {
            log.error("注册中心连接异常：Zookeeper session超时");
            while (true){
                log.info("重新连接注册中心...");
                boolean reconnected = doReconnect(client);
                if (reconnected){
                    break;
                }
            }
        }
        log.info("注册中心重连：成功重新连接 zk 注册中心...");
    }

    protected abstract boolean doReconnect(CuratorFramework curatorFramework);
}
