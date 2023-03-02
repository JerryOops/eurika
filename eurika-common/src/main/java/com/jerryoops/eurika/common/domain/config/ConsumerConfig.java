package com.jerryoops.eurika.common.domain.config;

import com.jerryoops.eurika.common.constant.PropertyConstant;
import lombok.Getter;

/**
 * 用来存放eurika.properties中，与调用方(consumer)相关的配置信息。
 */
@Getter
public class ConsumerConfig extends Config {

    {
        super.prefix = PropertyConstant.CONSUMER_CONFIG_PREFIX;
    }

    /**
     * consumer进行同步调用时，最大等待时间。仅对@EurikaReference中async设为false的同步调用生效。
     * <p>key = {super.prefix} + timeout.invocation </p>
     * <p>value = an integer with millisecond as the time unit </p>
     * <p>defaultValue = 20,000 </p>
     */
    private Integer timeoutInvocation = 20000;
}
