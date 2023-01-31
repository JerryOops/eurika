package com.jerryoops.eurika.common.util;

import com.jerryoops.eurika.common.domain.ServiceInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.jerryoops.eurika.common.constant.ZookeeperConstant.*;

@Component
public class ZookeeperUtil {

    public String buildProviderPath(ServiceInfo serviceInfo) {
        return this.buildProviderPath(
                serviceInfo.getGroup(),
                serviceInfo.getServiceName(),
                serviceInfo.getVersion(),
                serviceInfo.getHost() + serviceInfo.getPort()
        );
    }

    public String buildProviderPath(String group, String serviceName, String version, String address) {
        return EURIKA_ROOT_PATH
                + PATH_SEPARATOR + group
                + PATH_SEPARATOR + serviceName
                + PATH_SEPARATOR + PROVIDERS
                + PATH_SEPARATOR + version
                + PATH_SEPARATOR + address;
    }
}
