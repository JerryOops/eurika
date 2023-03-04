package com.jerryoops.eurika.common.util;

import com.jerryoops.eurika.common.constant.ZookeeperConstant;
import com.jerryoops.eurika.common.domain.ProviderLeafNode;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import org.springframework.stereotype.Component;

import static com.jerryoops.eurika.common.constant.ZookeeperConstant.EURIKA_ROOT_PATH;
import static com.jerryoops.eurika.common.constant.ZookeeperConstant.PROVIDERS;
import static com.jerryoops.eurika.common.constant.ZookeeperConstant.PATH_SEPARATOR;

@Component
public class ZookeeperUtil {

    public static String buildRoleDepth(String group, String serviceName) {
        return EURIKA_ROOT_PATH
                + PATH_SEPARATOR + StringEscapeUtil.escapeSlash(group)
                + PATH_SEPARATOR + StringEscapeUtil.escapeSlash(serviceName)
                + PATH_SEPARATOR + PROVIDERS;
    }


    /**
     * 用于将ServiceInfo POJO对象转变为注册中心（zookeeper实现）中的一条路径。
     * 调用前需保证serviceInfo、serviceInfo.annotationInfo均不为null。
     * @param serviceInfo
     * @return
     */
    public static String buildFullDepthPath(ServiceInfo serviceInfo) {
        ProviderLeafNode leafNode = ProviderLeafNode.builder()
                .host(serviceInfo.getHost())
                .port(serviceInfo.getPort())
                .version(serviceInfo.getVersion())
                .build();
        return buildFullDepthPath(
                serviceInfo.getGroup(),
                serviceInfo.getServiceName(),
                ProviderLeafNode.stringify(leafNode)
        );
    }


    /**
     * buildFullDepthPath的逆方法，将path解析为ServiceInfo实例。
     * @param path full-depth path,
     *             e.g. /eurika/DEFAULT_GROUP/com.jerryoops.HelloService/providers/127.0.0.1:2019:1.0.0
     * @return
     */
    public static ServiceInfo parseFullDepthPath(String path) {
        // ["", eurika, group, serviceName, "providers", providerLeafNodeString]
        String[] splitPath = path.split(PATH_SEPARATOR);
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setGroup(StringEscapeUtil.unescape(splitPath[2]));
        serviceInfo.setServiceName(StringEscapeUtil.unescape(splitPath[3]));
        ProviderLeafNode leafNode = ProviderLeafNode.parse(StringEscapeUtil.unescape(splitPath[5]));
        serviceInfo.setHost(leafNode.getHost());
        serviceInfo.setPort(leafNode.getPort());
        serviceInfo.setVersion(leafNode.getVersion());
        return serviceInfo;
    }


    private static String buildFullDepthPath(String group, String serviceName, String providerLeafNodeString) {
        return EURIKA_ROOT_PATH
                + PATH_SEPARATOR + StringEscapeUtil.escapeSlash(group)
                + PATH_SEPARATOR + StringEscapeUtil.escapeSlash(serviceName)
                + PATH_SEPARATOR + PROVIDERS
                + PATH_SEPARATOR + StringEscapeUtil.escapeSlash(providerLeafNodeString);
    }
}
