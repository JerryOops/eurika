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
}
