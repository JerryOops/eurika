package com.jerryoops.eurika.common.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 用于存放eurika-start下的配置文件信息
 */
@Getter
@Configuration
@PropertySource("classpath:eurika.properties")
public class SpecifiedConfig {

    /**
     * 注册中心地址 host:port
     */
    @Value("${eurika.registry.address}")
    private String registryAddress;

    @Value("${eurika.registry.timeout.connection:15000}")
    private Integer registryConnectionTimeoutMilliseconds;

    @Value("${eurika.registry.timeout.session:60000}")
    private Integer registrySessionTimeoutMilliseconds;
}
