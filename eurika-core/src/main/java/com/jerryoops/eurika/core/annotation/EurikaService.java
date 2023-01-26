package com.jerryoops.eurika.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在对外提供的接口的实现类之上，其它服务可通过Eurika调用之
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface EurikaService {
    String version() default "1.0.0";
}
