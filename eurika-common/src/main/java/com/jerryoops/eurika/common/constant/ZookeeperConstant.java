package com.jerryoops.eurika.common.constant;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class ZookeeperConstant {
    public static final Integer DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = 15000;
    public static final Integer DEFAULT_SESSION_TIMEOUT_MILLISECONDS = 60000;

    public static final String EURIKA_ROOT_PATH = "/eurika";
    public static final String PATH_SEPARATOR = "/";
    public static final String PROVIDERS = "providers";
    /**
     * 在provider leafnode中，用于分割每个属性的分隔符。
     * 属性值内部存在的"&"在字符串化(stringify)的过程中将会被转义为"\u0026"
     */
    public static final String PROVIDER_LEAFNODE_ATTRIBUTE_SEPARATOR = "&";
    public static final String ESCAPED_PROVIDER_LEAFNODE_ATTRIBUTE_SEPARATOR = "\\u0026";
}
