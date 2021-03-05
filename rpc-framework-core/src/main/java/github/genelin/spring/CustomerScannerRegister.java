package github.genelin.spring;

import github.genelin.annotation.RpcScan;
import github.genelin.annotation.RpcService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * 自定义包扫描器注册器 由@import导入
 *
 * @author gene lin
 * @createTime 2021/3/5 14:08
 */
@Slf4j
public class CustomerScannerRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final String SPRING_BEAN_BASE_PACKAGE = "github.genelin.spring";
    private static final String BASE_PACKAGES_ATTRIBUTE_NAME = "basePackages";
    private static final String BASE_PACKAGE_CLASSES_ATTRIBUTE_NAME = "basePackageClasses";

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取注解@RpcScan的属性值
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes
            .fromMap(importingClassMetadata.getAnnotationAttributes(RpcScan.class.getName()));

        List<String> basePackages = new ArrayList<>();
        //取到所有属性的值
        basePackages.addAll(Arrays.stream(rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGES_ATTRIBUTE_NAME)).filter(StringUtils::hasText).collect(Collectors.toList()));
        basePackages.addAll(Arrays.stream(rpcScanAnnotationAttributes.getClassArray(BASE_PACKAGE_CLASSES_ATTRIBUTE_NAME)).map(ClassUtils::getPackageName).collect(Collectors.toList()));

        // Scan the RpcService annotation
        CustomScanner rpcServiceScanner = new CustomScanner(registry, RpcService.class);
        // Scan the Component annotation
        CustomScanner springBeanScanner = new CustomScanner(registry, Component.class);
        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        int springBeanAmount = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("springBeanScanner扫描的数量 [{}]", springBeanAmount);
        int rpcServiceCount = basePackages.size() > 0 ? rpcServiceScanner.scan(StringUtils.toStringArray(basePackages)) : 0;
        log.info("rpcServiceScanner扫描的数量 [{}]", rpcServiceCount);
    }
}
