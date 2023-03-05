package com.jerryoops.eurika.transmission.functioner;

import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.spring.annotation.EurikaService;
import com.jerryoops.eurika.common.util.ApplicationContextUtil;
import com.jerryoops.eurika.common.util.StringEscapeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.jerryoops.eurika.common.constant.ProviderConstant.SERVICE_BEAN_MAP_KEY_SEPARATOR;

/**
 * 持有本地所有标注了@EurikaService的bean实例，在接收到RPC call时从此处获取bean、并进行调用。
 */
@Slf4j
@Component
public class ServiceHolder {

    // name#group#version(service specification) --> beanObject
    private Map<String, Object> serviceBeanMap;

    /**
     * 本类初始化方法。SmartLifecycle保证该方法会在所有的bean都初始化完成之后被调用，用来初始化本类实例。
     * 将会从Spring IOC容器中获取所有被@EurikaService标注的类实例，并持有指向所有该类实例的引用；
     */
    @PostConstruct
    private void start() {
        Map<String, Object> map = ApplicationContextUtil.getBeanMapWithAnnotation(EurikaService.class);
        this.buildServiceBeanMap(map);
    }

    /**
     * 构建serviceBeanMap。
     * 检查是否出现如下非法情况：<p>
     * 1. 一个被标注了@EurikaService的类直接实现了多于1个的接口
     *      （处理：以warn日志打印，将其从serviceBeanMap中移除）<p>
     * 2. 两个或多个被标注了@EurikaService的类，同时实现了1个相同的接口，且group、version参数完全相同
     *      （处理：以warn日志打印，随机选择一个实现，将其它实现从serviceBeanMap移除）<p>
     * @param map (service实现类短名称 -> beanObject)
     */
    private void buildServiceBeanMap(Map<String, Object> map) {
        this.serviceBeanMap = new ConcurrentHashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String beanName = entry.getKey();
            Object beanObject = entry.getValue();
            // 情况1
            Class<?>[] interfaces = beanObject.getClass().getInterfaces();
            if (interfaces.length > 1) {
                log.warn("It is prohibited to have a service class annotated with @EurikaService that implements multiple interfaces. " +
                        "This service WILL NOT be registered and therefore WILL NOT be able to be called remotely: {}", beanName);
                continue;
            }
            // interfacesAmount <= 1
            // 如果该类没有实现任何接口，则使用该类的全限定名；否则使用该类的唯一直接实现的接口之全限定名
            String className = (interfaces.length == 1) ? interfaces[0].getName() : beanObject.getClass().getName();
            EurikaService annotation = ApplicationContextUtil.findAnnotationOnBean(beanName, EurikaService.class);
            String group = annotation.group();
            String version = annotation.version();
            // serviceBeanMap的key值，唯一则合法。若serviceBeanMap中已有相同的key值，说明出现了情况2
            String key = this.generateKey(className, group, version);
            if (serviceBeanMap.containsKey(key)) {
                log.warn("It is prohibited to have more than two service classes that implement the same interface " +
                        "and have exactly the same @EurikaService annotation attributes. " +
                        "This service WILL NOT be registered and therefore WILL NOT be able to be called remotely: {}", beanName);
                continue;
            }
            serviceBeanMap.put(key, beanObject);
        }
        log.info("serviceBeanMap = {}", serviceBeanMap);
    }

    public List<Object> getAllBeans() {
        return new ArrayList<>(serviceBeanMap.values());
    }


    /**
     * 根据给定的className,group,version获取key(name#group#version)，进一步在serviceBeanMap中获取对应的bean object。
     * @param className
     * @param group
     * @param version
     * @return
     * @throws EurikaException 当serviceBeanMap中不存在与入参key值匹配的对象时，抛出该异常。
     */
    public Object getServiceBean(String className, String group, String version) throws EurikaException {
        String key = this.generateKey(className, group, version);
        Object bean;
        if ((bean = serviceBeanMap.get(key)) == null) {
            throw EurikaException.fail(ResultCode.EXCEPTION_INACCESSIBLE_CALL, "Bean not found for key: " + key);
        }
        return bean;
    }


    /**
     * 将入参进行转义(仅对#进行转义)后，添加'#'分隔符，生成serviceBeanMap的key
     * @param className 未转义className
     * @param group 未转义group
     * @param version 未转义version
     * @return
     */
    private String generateKey(String className, String group, String version) {
        return StringEscapeUtil.escapeHashKey(className) + SERVICE_BEAN_MAP_KEY_SEPARATOR +
                StringEscapeUtil.escapeHashKey(group) + SERVICE_BEAN_MAP_KEY_SEPARATOR +
                StringEscapeUtil.escapeHashKey(version);
    }
}
