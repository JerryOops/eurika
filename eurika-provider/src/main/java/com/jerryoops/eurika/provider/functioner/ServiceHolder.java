package com.jerryoops.eurika.provider.functioner;

import com.jerryoops.eurika.common.spring.context.annotation.EurikaService;
import com.jerryoops.eurika.common.util.ApplicationContextUtil;
import com.jerryoops.eurika.common.util.StringEscapeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

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
public class ServiceHolder implements SmartLifecycle {

    private boolean runningFlag = false;

    // name#group#version(service specification) --> beanObject
    private Map<String, Object> serviceMap;

    /**
     * 本类初始化方法。SmartLifecycle保证该方法会在所有的bean都初始化完成之后被调用，用来初始化本类实例。
     * 将会从Spring IOC容器中获取所有被@EurikaService标注的类实例，并持有指向所有该类实例的引用；
     */
    @Override
    public void start() {
        Map<String, Object> map = ApplicationContextUtil.getBeanMapWithAnnotation(EurikaService.class);
        this.buildServiceMap(map);
        setRunningFlag(true);
    }

    @Override
    public void stop() {
        // do nothing,
        // since implementation of SmartLifecycle will not have this method called
    }

    @Override
    public boolean isRunning() {
        return runningFlag;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        callback.run();
        setRunningFlag(false);
    }

    private void setRunningFlag(boolean runningFlag) {
        this.runningFlag = runningFlag;
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
            String name = (interfaces.length == 1) ? interfaces[0].getCanonicalName() : beanObject.getClass().getCanonicalName();
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


    public List<String> getServiceMapKeys() {
        return new ArrayList<>(serviceMap.keySet());
    }
}
