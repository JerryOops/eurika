package com.jerryoops.eurika.provider.functioner;

import cn.hutool.core.util.RuntimeUtil;
import com.jerryoops.eurika.registry.register.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServiceDeregistar {

    @Autowired
    RegistryService registryService;

    /**
     * 向JVM Runtime添加ShutdownHook，在虚拟机停机前移除所有已注册到注册中心的服务信息。
     */
    public void addShutdownHook() {
        RuntimeUtil.addShutdownHook(() -> {
            registryService.deregisterAll();
        });
    }
}
