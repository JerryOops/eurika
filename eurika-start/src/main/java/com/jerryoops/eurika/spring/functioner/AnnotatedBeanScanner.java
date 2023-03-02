package com.jerryoops.eurika.spring.functioner;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.annotation.Annotation;

public class AnnotatedBeanScanner extends ClassPathBeanDefinitionScanner {

    public AnnotatedBeanScanner(BeanDefinitionRegistry beanDefinitionRegistry, Class<? extends Annotation> annotationClazz) {
        super(beanDefinitionRegistry);
        super.addIncludeFilter(new AnnotationTypeFilter(annotationClazz));
    }
}
