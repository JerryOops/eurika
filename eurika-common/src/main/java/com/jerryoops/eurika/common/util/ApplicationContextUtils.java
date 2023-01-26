package com.jerryoops.eurika.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Map;

@Component
public class ApplicationContextUtils implements ApplicationContextAware {
    private static ApplicationContext ctx;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    public static Map<String, Object> getBeanMapWithAnnotation(
            Class<? extends Annotation> annotationClazz) {
        return ctx.getBeansWithAnnotation(annotationClazz);
    }
}
