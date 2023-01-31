package com.jerryoops.eurika.common.constant;

import org.springframework.stereotype.Component;

@Component
public class RegistryConstant {
    /**
     * 默认的服务注册最大等待时间，10000毫秒（即10秒）
     */
    public static final Integer DEFAULT_MAX_WAIT_MILLISECONDS = 10000;

    public static final String DEFAULT_GROUP_NAME = "DEFAULT_GROUP";

    public static final String DEFAULT_VERSION = "1.0.0";
}
