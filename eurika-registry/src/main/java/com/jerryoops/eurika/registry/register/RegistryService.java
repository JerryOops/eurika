package com.jerryoops.eurika.registry.register;

import com.jerryoops.eurika.common.domain.ServiceAnnotationInfo;
import com.jerryoops.eurika.common.domain.ServiceInfo;

import java.util.List;

public interface RegistryService {
    /**
     * 将serviceInfo注册到注册中心。
     * @param serviceInfoList
     */
    void register(List<ServiceInfo> serviceInfoList);

    /**
     * 将所有已注册到注册中心的服务从注册中心中撤除。
     */
    void deregisterAll();

    /**
     * 由于Curator connection loss等原因，需要重新将本机的服务注册到注册中心中时使用。
     */
    void reregisterAll();
}
