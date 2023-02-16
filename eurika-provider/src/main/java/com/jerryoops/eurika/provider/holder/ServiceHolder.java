package com.jerryoops.eurika.provider.holder;

import com.jerryoops.eurika.common.spring.context.annotation.EurikaService;
import com.jerryoops.eurika.common.util.ApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 持有本地所有标注了@EurikaService的bean实例，在接收到RPC call时从此处获取bean、并进行调用。
 */
@Slf4j
@Component
public class ServiceHolder implements SmartLifecycle {

    private boolean runningFlag = false;

    // beanName --> beanObject
    private Map<String, Object> serviceMap;

    /**
     * SmartLifecycle保证该方法会在所有的bean都初始化完成之后被调用，用来初始化本类实例。
     * 将会从Spring IOC容器中获取所有被@EurikaService标注的类实例，并持有指向所有该类实例的引用。
     */
    @Override
    public void start() {
        this.serviceMap = ApplicationContextUtil.getBeanMapWithAnnotation(EurikaService.class);
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
}
