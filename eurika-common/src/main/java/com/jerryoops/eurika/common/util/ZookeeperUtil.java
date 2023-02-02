package com.jerryoops.eurika.common.util;

import com.jerryoops.eurika.common.domain.ProviderLeafNode;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import org.springframework.stereotype.Component;

import static com.jerryoops.eurika.common.constant.ZookeeperConstant.EURIKA_ROOT_PATH;
import static com.jerryoops.eurika.common.constant.ZookeeperConstant.PROVIDERS;
import static com.jerryoops.eurika.common.constant.ZookeeperConstant.PATH_SEPARATOR;

@Component
public class ZookeeperUtil {

    public static String buildProviderPath(ServiceInfo serviceInfo) {
        String providerLeafNode = ProviderLeafNode.stringify(
                ProviderLeafNode.builder()
                        .host(serviceInfo.getHost())
                        .port(serviceInfo.getPort())
                        .version(serviceInfo.getVersion())
                        .build()
        );
        return buildProviderPath(
                serviceInfo.getGroup(),
                serviceInfo.getServiceName(),
                providerLeafNode
        );
    }

    private static String buildProviderPath(String group, String serviceName, String providerLeafNode) {
        return EURIKA_ROOT_PATH
                + PATH_SEPARATOR + group
                + PATH_SEPARATOR + serviceName
                + PATH_SEPARATOR + PROVIDERS
                + PATH_SEPARATOR + providerLeafNode;
    }
}
