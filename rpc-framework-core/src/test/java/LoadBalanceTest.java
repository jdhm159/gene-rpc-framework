import com.sun.javafx.fxml.expression.KeyPath;
import github.genelin.common.entity.RpcServiceProperties;
import github.genelin.loadbalance.LoadBalance;
import github.genelin.loadbalance.loadbalancer.RandomLoadBalancer;
import github.genelin.registry.zookeeper.ZookeeperServiceDiscovery;
import github.genelin.registry.zookeeper.listener.ServiceRegistryConnectionListener;
import github.genelin.registry.zookeeper.util.CuratorUtil;
import github.genelin.remoting.constants.RpcConstants;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author gene lin
 * @createTime 2020/12/30 16:41
 */
public class LoadBalanceTest {

    @Test
    public void randomLoadBalancerTestWithCurator() throws UnknownHostException {
        CuratorUtil.createClient(new ServiceRegistryConnectionListener());
        RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder().interfaceName("gene.TestService").build();
        String rpcServiceName = rpcServiceProperties.toRPCServiceName();
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        InetSocketAddress url1 = new InetSocketAddress(hostAddress
            , RpcConstants.DEFAULT_PORT);
        InetSocketAddress url2 = new InetSocketAddress("127.0.0.1", 21255);
        CuratorUtil.createEphemeralNode(rpcServiceName, url1);
        CuratorUtil.createEphemeralNode(rpcServiceName, url2);

        RandomLoadBalancer randomLoadBalancer = new RandomLoadBalancer();
        InetSocketAddress inetSocketAddress = new ZookeeperServiceDiscovery().lookupService(rpcServiceProperties, randomLoadBalancer);
        System.out.println(inetSocketAddress);

        inetSocketAddress = new ZookeeperServiceDiscovery().lookupService(rpcServiceProperties, randomLoadBalancer);
        System.out.println(inetSocketAddress);

        inetSocketAddress = new ZookeeperServiceDiscovery().lookupService(rpcServiceProperties, randomLoadBalancer);
        System.out.println(inetSocketAddress);
    }

    @Test
    public void randomLoadBalancerTest(){
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        LoadBalance loadBalance = new RandomLoadBalancer();
        for (int i = 0; i < 5; i++) {
            System.out.println(loadBalance.select(list));
        }
    }

}
