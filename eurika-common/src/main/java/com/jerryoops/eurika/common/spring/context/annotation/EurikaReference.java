package com.jerryoops.eurika.common.spring.context.annotation;

import com.jerryoops.eurika.common.constant.RegistryConstant;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在consumer端使用，标注在某个希望寄托给Eurika进行远程调用的Service类变量上。
 * 将会使用Eurika进行远程服务调用。
 */
@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EurikaReference {
    /**
     * 用于标识被调用类从属的组名。
     * <p>不显式指定时，默认为"DEFAULT_GROUP"。</p>
     */
    String group() default RegistryConstant.DEFAULT_GROUP_NAME;

    /**
     * 用于标识被调用类的版本。
     * <p>不显式指定时，默认为"1.0.0"。</p>
     */
    String version() default RegistryConstant.DEFAULT_VERSION;
}
