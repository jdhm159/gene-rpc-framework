package github.genelin.loadbalance;

import java.util.List;

/**
 * @author gene lin
 * @createTime 2020/12/30 20:23
 */
public abstract class AbstractLoadBalancer implements LoadBalance {

    @Override
    public String select(List<String> providers) {
        if (providers == null || providers.size() == 0) {
            return null;
        }
        String result;
        if (providers.size() == 1) {
            result = providers.get(0);
        } else {
            result = doSelect(providers);
        }
        return result;
    }

    public abstract String doSelect(List<String> providers);
}
