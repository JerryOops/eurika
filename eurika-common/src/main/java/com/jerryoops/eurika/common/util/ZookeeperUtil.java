package com.jerryoops.eurika.common.util;

import com.jerryoops.eurika.common.domain.ProviderServiceInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static com.jerryoops.eurika.common.constant.RegistryConstant.DEFAULT_GROUP_NAME;
import static com.jerryoops.eurika.common.constant.RegistryConstant.DEFAULT_VERSION;
import static com.jerryoops.eurika.common.constant.ZookeeperConstant.*;

@Component
public class ZookeeperUtil {

    public String buildProviderPath(ProviderServiceInfo providerServiceInfo) {
        return this.buildProviderPath(
                providerServiceInfo.getGroup(),
                providerServiceInfo.getServiceName(),
                providerServiceInfo.getVersion(),
                providerServiceInfo.getHost() + providerServiceInfo.getPort()
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
