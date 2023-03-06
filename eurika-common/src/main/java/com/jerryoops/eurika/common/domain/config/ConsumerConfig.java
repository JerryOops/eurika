package com.jerryoops.eurika.common.domain.config;

import com.jerryoops.eurika.common.constant.PropertyConstant;
import com.jerryoops.eurika.common.enumeration.LoadBalanceEnum;
import lombok.Getter;
import lombok.ToString;

/**
 * 用来存放eurika.properties中，与调用方(consumer)相关的配置信息。
 */
@Getter
@ToString
public class ConsumerConfig extends Config {

    {
        super.prefix = PropertyConstant.CONSUMER_CONFIG_PREFIX;
    }

    /**
     * consumer进行同步调用时，最大等待时间。仅对@EurikaReference中async设为false的同步调用生效。
     * <p>key = {super.prefix} + .timeout.invocation </p>
     * <p>value = an integer with millisecond as the time unit </p>
     * <p>defaultValue = 20,000 </p>
     */
    private Integer timeoutInvocation = 20000;

    /**
     * consumer在选取provider进行RPC调用时的负载均衡策略。
     * <p>key = {super.prefix} + .loadbalance </p>
     * <p>value = a string that represents a specific strategy of load balance </p>
     * <p>defaultValue = random </p>
     */
    private String loadbalance = LoadBalanceEnum.RANDOM.getName();
}
