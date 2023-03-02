package com.jerryoops.eurika.spring.annotation;

import com.jerryoops.eurika.spring.functioner.EurikaBeanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在启动类之上，用于启动Eurika服务。
 * 将会进行服务扫描、注册、发现等一系列初始化动作。
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(EurikaBeanRegistrar.class)
public @interface EnableEurika {
    /**
     * 用于标识需要扫描的包路径。无论是服务调用还是暴露，只有该指定路径下的类会被Eurika处理。
     * 如未显式指定该路径，则将会使用被@EnableEurika标识的类的包路径填充该值。
     */
    String[] packagePaths() default {};
}
