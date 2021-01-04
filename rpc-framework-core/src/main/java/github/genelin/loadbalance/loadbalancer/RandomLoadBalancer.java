package github.genelin.loadbalance.loadbalancer;

import github.genelin.loadbalance.AbstractLoadBalancer;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gene lin
 * @createTime 2020/12/21 16:10
 */
public class RandomLoadBalancer extends AbstractLoadBalancer {

    @Override
    public String doSelect(List<String> providers) {
        return providers.get(new Random().nextInt(providers.size()));
    }
}

