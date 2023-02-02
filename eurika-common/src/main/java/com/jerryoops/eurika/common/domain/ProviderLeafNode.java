package com.jerryoops.eurika.common.domain;

import com.jerryoops.eurika.common.util.JsonUtil;
import lombok.Builder;
import lombok.Data;

/**
 * zookeeper中/providers路径下的节点值对应的POJO。
 * e.g. zookeeper path = /eurika/DEFAULT_GROUP/com.jerryoops.DemoService/providers/198.1.1.34:8091?1.0.1
 * 对应ProviderLeafNode: host=198.1.1.34, port=8091, version=1.0.1
 */
@Data
@Builder
public class ProviderLeafNode {
    private String host;
    private Integer port;
    private String version;

    public static String stringify(ProviderLeafNode leafNode) {
        return JsonUtil.toJson(leafNode);
    }

    public static ProviderLeafNode parse(String s) {
        return JsonUtil.fromJson(s, ProviderLeafNode.class);
    }
}
