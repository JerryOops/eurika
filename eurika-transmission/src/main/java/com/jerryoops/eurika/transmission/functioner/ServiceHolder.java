package com.jerryoops.eurika.transmission.functioner;

import com.jerryoops.eurika.common.constant.ProviderConstant;
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

import static com.jerryoops.eurika.common.constant.ProviderConstant.SERVICE_MAP_KEY_SEPARATOR;

/**
 * 持有本地所有标注了@EurikaService的bean实例，在接收到RPC call时从此处获取bean、并进行调用。
 */
@Slf4j
@Component
public class ServiceHolder {

    // name#group#version(service specification) --> beanObject
    private Map<String, Object> serviceMap;

    /**
     * 本类初始化方法。SmartLifecycle保证该方法会在所有的bean都初始化完成之后被调用，用来初始化本类实例。
     * 将会从Spring IOC容器中获取所有被@EurikaService标注的类实例，并持有指向所有该类实例的引用；
     */
    @PostConstruct
    private void start() {
        Map<String, Object> map = ApplicationContextUtil.getBeanMapWithAnnotation(EurikaService.class);
        this.buildServiceMap(map);
    }

    /**
     * 构建serviceMap。
     * 检查是否出现如下非法情况：<p>
     * 1. 一个被标注了@EurikaService的类直接实现了多于1个的接口
     *      （处理：以warn日志打印，将其从serviceMap中移除）<p>
     * 2. 两个或多个被标注了@EurikaService的类，同时实现了1个相同的接口，且group、version参数完全相同
     *      （处理：以warn日志打印，随机选择一个实现，将其它实现从serviceMap移除）<p>
     * @param map (service实现类短名称 -> beanObject)
     */
    private void buildServiceMap(Map<String, Object> map) {
        this.serviceMap = new ConcurrentHashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String beanName = entry.getKey();
            Object beanObject = entry.getValue();
            // 情况1
            Class<?>[] interfaces = beanObject.getClass().getInterfaces();
            int interfacesAmount = interfaces.length;
            if (interfacesAmount > 1) {
                log.warn("It is prohibited to have a service class annotated with @EurikaService that implements multiple interfaces. " +
                        "This service WILL NOT be registered and therefore WILL NOT be able to be called remotely: {}", beanName);
                continue;
            }
            // interfacesAmount <= 1
            // 如果该类没有实现任何接口，则使用该类的全限定名；否则使用该类的唯一直接实现的接口之全限定名
            String name = (interfaces.length == 1) ? interfaces[0].getName() : beanObject.getClass().getName();
            EurikaService annotation = ApplicationContextUtil.findAnnotationOnBean(beanName, EurikaService.class);
            String group = annotation.group();
            String version = annotation.version();
            // serviceMap的key值，唯一则合法。若serviceMap中已有相同的key值，说明出现了情况2
            String key = StringEscapeUtil.escapeHashKey(name) + SERVICE_MAP_KEY_SEPARATOR +
                    StringEscapeUtil.escapeHashKey(group) + SERVICE_MAP_KEY_SEPARATOR +
                    StringEscapeUtil.escapeHashKey(version);
            if (serviceMap.containsKey(key)) {
                log.warn("It is prohibited to have more than two service classes that implement the same interface " +
                        "and have exactly the same @EurikaService annotation attributes. " +
                        "This service WILL NOT be registered and therefore WILL NOT be able to be called remotely: {}", beanName);
                continue;
            }
            serviceMap.put(key, beanObject);
        }
        log.info("serviceMap = {}", serviceMap);
    }

    public List<Object> getAllBeans() {
        return new ArrayList<>(serviceMap.values());
    }


    /**
     * 根据给定的className,group,version获取key(name#group#version)，进一步在serviceMap中获取对应的bean object。
     * @param className
     * @param group
     * @param version
     * @return
     * @throws EurikaException 当serviceMap中不存在与入参key值匹配的对象时，抛出该异常。
     */
    public Object getServiceBean(String className, String group, String version) throws EurikaException {
        String key = this.getKey(className, group, version);
        Object bean;
        if ((bean = serviceMap.get(key)) == null) {
            throw EurikaException.fail(ResultCode.EXCEPTION_INACCESSIBLE_CALL, "Bean not found for key: " + key);
        }
        return bean;
    }

    private String getKey(String className, String group, String version) {
        return StringEscapeUtil.unescape(className) + ProviderConstant.SERVICE_MAP_KEY_SEPARATOR +
                StringEscapeUtil.unescape(group) + ProviderConstant.SERVICE_MAP_KEY_SEPARATOR +
                StringEscapeUtil.unescape(version);
    }
}
