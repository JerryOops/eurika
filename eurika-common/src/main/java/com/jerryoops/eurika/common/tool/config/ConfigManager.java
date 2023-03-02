package com.jerryoops.eurika.common.tool.config;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.resource.NoResourceException;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import cn.hutool.setting.SettingUtil;
import com.jerryoops.eurika.common.constant.PropertyConstant;
import com.jerryoops.eurika.common.domain.config.Config;
import com.jerryoops.eurika.common.domain.config.ConsumerConfig;
import com.jerryoops.eurika.common.domain.config.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ConfigManager {
    private static Setting setting;

    private static final Map<Class<?>, Config> configCacheMap;

    static {
        try {
            setting = SettingUtil.get(PropertyConstant.PROPERTY_FILE_NAME);
        } catch (NoResourceException e) {
            log.warn("Configuration file eurika.properties is not specified, will use default config instead");
        }
        configCacheMap = new ConcurrentHashMap<>(4);
    }

    // ------------------------------外部方法------------------------------
    public static RegistryConfig getRegistryConfig() {
        return (RegistryConfig) get(RegistryConfig.class);
    }

    public static ConsumerConfig getConsumerConfig() {
        return (ConsumerConfig) get(ConsumerConfig.class);
    }



    // -------------------------------内部方法-----------------------------

    /**
     * 从configCacheMap中获取对应的配置类实例。如果获取不到，则调用load()方法加载并放入configCacheMap中。
     * @param configClazz
     * @return
     * @param <T>
     */
    private static <T extends Config> Config get(Class<T> configClazz) {
        return configCacheMap.computeIfAbsent(configClazz, value -> load(configClazz));
    }

    /**
     * 根据给定的configClass，加载并返回对应的配置类实例
     * @param configClazz
     * @return
     * @param <T>
     */
    private static <T extends Config> Config load(Class<T> configClazz) {
        try {
            Config config = configClazz.newInstance();
            for (Field field : configClazz.getDeclaredFields()) {
                // ignore static fields
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                // for field that are non-static, generate its property key (the key used to get property value in properties file)
                field.setAccessible(true);
                String propertyKey = config.getPrefix() + CharUtil.DOT + StrUtil.toSymbolCase(field.getName(), CharUtil.DOT);
                String propertyValue = getPropertyValue(propertyKey, field.get(config));
                if (null != propertyValue) {
                    // if specified in properties file, overwrite its default value in config object
                    field.set(config, Convert.convert(field.getType(), propertyValue));
                }
            }
            return config;

        } catch (Exception e) {
            throw new RuntimeException("Exception caught when loading config for " + configClazz, e);
        }
    }


    private static String getPropertyValue(String key, Object defaultValue) {
        String value;
        if (null != setting && StringUtils.isNotBlank(value = setting.get(key))) {
            return value;
        }
        log.info("Failed to load property with key '{}', will use its default value '{}' instead", key, defaultValue);
        return null;
    }
}
