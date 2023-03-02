package com.jerryoops.eurika.common.domain.config;

import lombok.Getter;

@Getter
public abstract class Config {
    /**
     * 由子类覆写。用于区分不同配置子类的前缀。
     */
    protected String prefix;
}
