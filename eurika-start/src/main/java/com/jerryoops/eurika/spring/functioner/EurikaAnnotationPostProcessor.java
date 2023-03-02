package com.jerryoops.eurika.spring.functioner;

import com.jerryoops.eurika.common.spring.annotation.EurikaReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Slf4j
@Component
public class EurikaAnnotationPostProcessor implements BeanPostProcessor {

    /**
     * afterInitialization:
     * <p>用于处理所有被@EurikaReference标注的域：构建其动态代理对象proxy，并使用proxy替代该域。</p>
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        for (Field declaredField : clazz.getDeclaredFields()) {
            EurikaReference annotationMetadata = declaredField.getAnnotation(EurikaReference.class);
            if (null == annotationMetadata) {
                continue;
            }
            // TODO: 2023/2/27 添加proxy
        }
        return bean;
    }
}
