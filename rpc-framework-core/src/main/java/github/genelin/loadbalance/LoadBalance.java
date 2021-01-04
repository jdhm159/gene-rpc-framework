package github.genelin.loadbalance;

import github.genelin.common.extension.SPI;
import java.util.List;

/**
 * @author gene lin
 * @createTime 2020/12/21 15:54
 */
@SPI("random")
public interface LoadBalance {

    String select(List<String> providers);
}
