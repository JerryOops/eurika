package com.jerryoops.eurika.spring.annotation;

import com.jerryoops.eurika.common.constant.RegistryConstant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在provider端使用，标注在某接口的实现类之上。
 * 将会把此实现类作为bean注册到Spring IOC容器中，并将此类实现的接口暴露在服务注册中心中，供消费者调用。
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EurikaService {
    /**
     * 用于标识本实现类从属的组名。
     * <p>不显式指定时，默认为"DEFAULT_GROUP"。</p>
     */
    String group() default RegistryConstant.DEFAULT_GROUP_NAME;

    /**
     * 用于标识本实现类的版本。
     * <p>不显式指定时，默认为"1.0.0"。</p>
     */
    String version() default RegistryConstant.DEFAULT_VERSION;
}
