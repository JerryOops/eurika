package com.jerryoops.eurika.consumer.functioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 负责定期调用serviceDiscoverer从注册中心拉取最新的服务信息、并维护一份存储在本地的服务信息表(缓存)。
 */
@Component
@Slf4j
public class ConnectionManager {

    @Autowired
    private ServiceDiscoverer serviceDiscoverer;
}
