package com.jerryoops.eurika.core.bootstrap;

import com.jerryoops.eurika.common.util.ApplicationContextUtils;
import com.jerryoops.eurika.core.annotation.EurikaService;

import java.util.Map;

/**
 * 扫描本服务中所有标注了@EurikaService的类，对其进行初始化处理
 */
public class ServiceBootstrap {

    /**
     * 返回所有标注了@EurikaService的类
     */
    public void registerService() {
        Map<String, Object> beanMap = ApplicationContextUtils.getBeanMapWithAnnotation(EurikaService.class);

    }
}
