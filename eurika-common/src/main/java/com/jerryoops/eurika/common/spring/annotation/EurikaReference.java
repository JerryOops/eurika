package com.jerryoops.eurika.common.spring.annotation;

import com.jerryoops.eurika.common.constant.RegistryConstant;
import com.jerryoops.eurika.common.domain.listener.CallbackListener;
import com.jerryoops.eurika.common.domain.listener.DefaultCallbackListener;

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

    /**
     * 当async==true时，将会在RPC调用发送成功时立即返回。
     * 当接收到来自provider端的RPC响应时，调用listener中的响应方法。
     */
    boolean async() default false;

    /**
     * 用于RPC调用完成时的回调。
     */
    Class<? extends CallbackListener> listener() default DefaultCallbackListener.class;
}
