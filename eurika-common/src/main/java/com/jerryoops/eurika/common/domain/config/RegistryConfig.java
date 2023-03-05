package com.jerryoops.eurika.common.domain.config;

import com.jerryoops.eurika.common.constant.PropertyConstant;
import lombok.Getter;
import lombok.ToString;

/**
 * 用来存放eurika.properties中，与注册中心(registry)相关的配置信息。
 */
@Getter
@ToString
public class RegistryConfig extends Config {

    {
        super.prefix = PropertyConstant.REGISTRY_CONFIG_PREFIX;
    }

    /**
     * 注册中心地址
     * <p>key = {super.prefix} + .address </p>
     * <p>value = host:port </p>
     * <p>defaultValue = 127.0.0.1:2181 </p>
     */
    private String address = "127.0.0.1:2181";

    /**
     * 连接注册中心时的connection超时时间
     * <p>key = {super.prefix} + .timeout.connection </p>
     * <p>value = an integer with millisecond as the time unit </p>
     * <p>defaultValue = 15,000 </p>
     */
    private Integer timeoutConnection = 15000;

    /**
     * 连接注册中心时的session超时时间
     * <p>key = {super.prefix} + .timeout.session </p>
     * <p>value = an integer with millisecond as the time unit </p>
     * <p>defaultValue = 60,000 </p>
     */
    private Integer timeoutSession = 60000;
}
