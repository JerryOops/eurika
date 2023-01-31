package com.jerryoops.eurika.common.constant;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
public class ZookeeperConstant {
    /**
     * 默认的服务注册最大等待时间，10000毫秒（即10秒）
     */
    public static final Integer DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS = 10000;
    public static final Integer DEFAULT_SESSION_TIMEOUT_MILLISECONDS = 5000;

    public static final String EURIKA_ROOT_PATH = "/eurika";
    public static final String PATH_SEPARATOR = "/";
    public static final String PROVIDERS = "providers";
}
