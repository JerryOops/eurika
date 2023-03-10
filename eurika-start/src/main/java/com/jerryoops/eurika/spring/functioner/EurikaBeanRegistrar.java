package com.jerryoops.eurika.spring.functioner;

import com.jerryoops.eurika.common.constant.ContextConstant;
import com.jerryoops.eurika.common.spring.annotation.EurikaService;
import com.jerryoops.eurika.spring.functioner.AnnotatedBeanScanner;
import com.jerryoops.eurika.spring.annotation.EnableEurika;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;

/**
 * 用于扫描并注册bean到Spring IOC容器中。这些bean包括被@EurikaService和@EurikaReference修饰的类。
 */
@Slf4j
public class EurikaBeanRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    ResourceLoader resourceLoader;

    /**
     * 从Spring IOC容器中获得ResourceLoader实例，并在registrar类中持有指向该实例的引用。
     * 本方法被先于registerBeanDefinitions而被调用（ImportBeanDefinitionRegistrar中保证）。
     * @param resourceLoader
     */
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * 获取待扫描包路径、并扫描其中被@EurikaReference和@EurikaService修饰的类，将其加入到Spring IOC容器中。
     * @param importingClassMetadata 被@EnableEurika修饰的类的元信息
     * @param registry IOC容器的bean注册中心
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String[] packagePathsToBeScanned = this.getPackagePaths(importingClassMetadata);
        this.scanBeans(packagePathsToBeScanned, registry);
    }

    /**
     * 获取待扫描的包路径。
     * 默认从@EnableEurika的packagePaths属性中取值。如为空，则使用被@EnableEurika所修饰的类的包路径。
     * @param importingClassMetadata 被@EnableEurika修饰的类的元信息
     * @return 待扫描的包路径。
     */
    private String[] getPackagePaths(AnnotationMetadata importingClassMetadata) {
        AnnotationAttributes enableEurikaAnnotationAttributes = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableEurika.class.getName())
        );
        String[] packagePaths = null;
        if (null != enableEurikaAnnotationAttributes) {
            packagePaths = enableEurikaAnnotationAttributes.getStringArray(
                    ContextConstant.FieldName.ENABLE_EURIKA_PACKAGE_PATHS);
        }
        if (null == packagePaths || packagePaths.length == 0) {
            // 未在@EnableEurika中显式指定packagePaths属性
            String packagePath = ((StandardAnnotationMetadata) importingClassMetadata)
                    .getIntrospectedClass()
                    .getPackage()
                    .getName();
            packagePaths = new String[]{packagePath};
        }
        return packagePaths;
    }

    /**
     * 在packagePaths指定的包路径下进行bean扫描，并注册到beanDefinitionRegistry中。
     * 需要将com.jerryoops.eurika路径下的所有bean（eurika框架内部的bean）扫描、加入Spring IOC容器。
     * @param packagePaths 待扫描的包路径
     * @param registry IOC容器的bean注册中心
     */
    private void scanBeans(String[] packagePaths, BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner eurikaInnerBeanScanner = new ClassPathBeanDefinitionScanner(registry);
        AnnotatedBeanScanner eurikaServiceAnnotatedBeanScanner = new AnnotatedBeanScanner(registry, EurikaService.class);
        if (null != resourceLoader) {
            eurikaInnerBeanScanner.setResourceLoader(resourceLoader);
            eurikaServiceAnnotatedBeanScanner.setResourceLoader(resourceLoader);
        }
        int innerBeanAmount = eurikaInnerBeanScanner.scan(ContextConstant.EURIKA_INNER_SUPREME_PACKAGE_PATH);
        int serviceAmount = eurikaServiceAnnotatedBeanScanner.scan(packagePaths);
        log.info("innerBeanAmount = {}, serviceAmount = {}", innerBeanAmount, serviceAmount);
    }
}