package com.jerryoops.eurika.registry.register.interfaces;

import com.jerryoops.eurika.common.domain.ServiceInfo;

public interface RegistryService {
    /**
     * 将serviceInfo注册到注册中心。
     * @param serviceInfo
     */
    void register(ServiceInfo serviceInfo);

    /**
     * 将所有已注册到注册中心的服务从注册中心中撤除。
     */
    void deregisterAll();

    /**
     * 由于Curator connection loss等原因，需要重新将本机的服务注册到注册中心中时使用。
     */
    void reregisterAll();
}
