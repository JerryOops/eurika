package com.jerryoops.eurika.provider.functioner;

import cn.hutool.core.util.RuntimeUtil;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.spring.annotation.EurikaService;
import com.jerryoops.eurika.provider.server.ProviderServer;
import com.jerryoops.eurika.registry.register.RegistryService;
import com.jerryoops.eurika.transmission.functioner.ServiceHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ServiceRegistrar {

    @Autowired
    ServiceHolder serviceHolder;
    @Autowired
    ProviderServer providerServer;
    @Autowired
    RegistryService registryService;


    /**
     * 执行时机：根据依赖关系，将会在serviceHolder、providerServer二者的@PostConstruct方法执行完成之后执行。
     */
    @PostConstruct
    private void init() {
        this.doRegister();
        this.addShutdownHook();
    }

    /**
     * 调用RegistryService.register方法，将所有被@EurikaService标注的类的服务信息注册到服务注册中心。
     */
    private void doRegister() {
        List<ServiceInfo> serviceInfoList = this.convertToServiceInfoList(
                serviceHolder.getAllBeans(), providerServer.getHost(), providerServer.getPort()
        );
        registryService.register(serviceInfoList);
    }

    private List<ServiceInfo> convertToServiceInfoList(List<Object> beans, String host, int port) {
        List<ServiceInfo> serviceInfoList = new ArrayList<>(beans.size());
        for (Object bean : beans) {
            EurikaService annotationMetadata = bean.getClass().getAnnotation(EurikaService.class);
            ServiceInfo serviceInfo = new ServiceInfo();
                Class<?>[] interfaces = bean.getClass().getInterfaces();
                String serviceName = (interfaces.length == 1) ? interfaces[0].getName() : bean.getClass().getName();
            serviceInfo.setServiceName(serviceName);
            serviceInfo.setGroup(annotationMetadata.group());
            serviceInfo.setVersion(annotationMetadata.version());
            serviceInfo.setHost(host);
            serviceInfo.setPort(port);
            serviceInfoList.add(serviceInfo);
        }
        return serviceInfoList;
    }

    /**
     * 向JVM Runtime添加ShutdownHook，在虚拟机停机前移除所有已注册到注册中心的服务信息。
     */
    private void addShutdownHook() {
        RuntimeUtil.addShutdownHook(() -> {
            registryService.deregisterAll();
        });
    }
}
