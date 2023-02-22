package com.jerryoops.eurika.provider.functioner;

import cn.hutool.core.util.RuntimeUtil;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.util.StringEscapeUtil;
import com.jerryoops.eurika.registry.register.RegistryService;
import com.jerryoops.eurika.transmission.functioner.ServiceHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.jerryoops.eurika.common.constant.ProviderConstant.SERVICE_MAP_KEY_SEPARATOR;

@Slf4j
@Component
public class ServiceRegistrar {

    @Autowired
    ServiceHolder serviceHolder;
    @Autowired
    RegistryService registryService;

    /**
     * 调用RegistryService.register方法，将所有被@EurikaService标注的类的服务信息注册到服务注册中心。
     */
    public void doRegister(String host, Integer port) {
        List<ServiceInfo> serviceInfoList = this.convertToServiceInfoList(serviceHolder.getServiceMapKeys(), host, port);
        registryService.register(serviceInfoList);
    }

    private List<ServiceInfo> convertToServiceInfoList(List<String> serviceMapKeys, String host, int port) {
        List<ServiceInfo> serviceInfoList = new ArrayList<>(serviceMapKeys.size());
        for (String key : serviceHolder.getServiceMapKeys()) {
            String[] splitKey = key.split(String.valueOf(SERVICE_MAP_KEY_SEPARATOR));
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceName(StringEscapeUtil.unescape(splitKey[0]));
            serviceInfo.setGroup(StringEscapeUtil.unescape(splitKey[1]));
            serviceInfo.setVersion(StringEscapeUtil.unescape(splitKey[2]));
            serviceInfo.setHost(host);
            serviceInfo.setPort(port);
            serviceInfoList.add(serviceInfo);
        }
        return serviceInfoList;
    }

    /**
     * 向JVM Runtime添加ShutdownHook，在虚拟机停机前移除所有已注册到注册中心的服务信息。
     */
    public void addShutdownHook() {
        RuntimeUtil.addShutdownHook(() -> {
            registryService.deregisterAll();
        });
    }
}
