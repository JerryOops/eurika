package com.jerryoops.eurika.provider.holder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 持有本地所有标注了@EurikaService的bean实例
 */
@Slf4j
@Component
public class EurikaServiceBeanHolder {

    /**
     * 在所有的bean都初始化完成之后被调用，用来初始化本类实例。
     * 将会从Spring IOC容器中获取所有被@EurikaService标注的类实例，并持有指向所有该类实例的引用。
     */
    private void init() {
        
    }
}
