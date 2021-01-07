package service;

import github.genelin.common.extension.SPI;

/**
 * @author gene lin
 * @createTime 2021/1/7 16:23
 */
@SPI("impl2")
public interface HelloService {

    String hello();
}