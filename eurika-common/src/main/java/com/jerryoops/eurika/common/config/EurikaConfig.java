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
public class EurikaConfig {

    /**
     * 注册中心地址 host:port
     */
    @Value("${eurika.registry.address}")
    private String registryAddress;

    /**
     * 连接注册中心(zk)未成功状态的最大维持时间
     */
    @Value("${eurika.registry.wait.millis}")
    private Integer registryMaxWaitMilliseconds;

}
