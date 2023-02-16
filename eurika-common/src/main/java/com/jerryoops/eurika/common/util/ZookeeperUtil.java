package com.jerryoops.eurika.common.util;

import com.jerryoops.eurika.common.domain.ProviderLeafNode;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import org.springframework.stereotype.Component;

import static com.jerryoops.eurika.common.constant.ZookeeperConstant.EURIKA_ROOT_PATH;
import static com.jerryoops.eurika.common.constant.ZookeeperConstant.PROVIDERS;
import static com.jerryoops.eurika.common.constant.ZookeeperConstant.PATH_SEPARATOR;

@Component
public class ZookeeperUtil {

    /**
     * 用于将ServiceInfo POJO对象转变为注册中心（zookeeper实现）中的一条路径。
     * 调用前需保证serviceInfo、serviceInfo.annotationInfo均不为null。
     * @param serviceInfo
     * @return
     */
    public static String buildProviderPath(ServiceInfo serviceInfo) {
        ProviderLeafNode leafNode = ProviderLeafNode.builder()
                .host(serviceInfo.getHost())
                .port(serviceInfo.getPort())
                .version(serviceInfo.getAnnotationInfo().getVersion())
                .build();
        return buildProviderPath(
                serviceInfo.getAnnotationInfo().getGroup(),
                serviceInfo.getAnnotationInfo().getServiceName(),
                ProviderLeafNode.stringify(leafNode)
        );
    }

    private static String buildProviderPath(String group, String serviceName, String providerLeafNodeString) {
        return EURIKA_ROOT_PATH
                + PATH_SEPARATOR + group
                + PATH_SEPARATOR + serviceName
                + PATH_SEPARATOR + PROVIDERS
                + PATH_SEPARATOR + providerLeafNodeString;
    }
}
