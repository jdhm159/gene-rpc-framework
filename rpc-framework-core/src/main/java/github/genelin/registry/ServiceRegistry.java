package github.genelin.registry;

import github.genelin.common.extension.SPI;

/**
 * @author gene lin
 * @createTime 2020/12/20 15:51
 */
@SPI("zookeeper")
public interface ServiceRegistry {

    /**
     * 进行服务注册
     *
     * @param serviceProperties 服务标识
     */
    void registry(String serviceProperties);
}
