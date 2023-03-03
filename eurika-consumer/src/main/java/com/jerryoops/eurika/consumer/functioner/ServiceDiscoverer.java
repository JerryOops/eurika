package com.jerryoops.eurika.consumer.functioner;

import com.jerryoops.eurika.registry.discover.DiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 负责与eurika-registry模块的DiscoveryService直接交互的实例。
 */
@Component
@Slf4j
public class ServiceDiscoverer {

    @Autowired
    private DiscoveryService discoveryService;
}
