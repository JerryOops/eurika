package com.jerryoops.eurika.spring.functioner;

import com.jerryoops.eurika.common.spring.annotation.EurikaReference;
import com.jerryoops.eurika.consumer.client.ConsumerClient;
import com.jerryoops.eurika.consumer.functioner.ConnectionManager;
import com.jerryoops.eurika.consumer.proxy.ReferencedServiceInvocationHandler;
import com.jerryoops.eurika.transmission.functioner.UnrespondedFutureHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

@Slf4j
@Component
public class EurikaAnnotationPostProcessor implements BeanPostProcessor {

    // RPC调用中，消费者一端的、负责发送和接收RPC信息的client实例
    @Autowired
    private ConsumerClient consumerClient;
    // 维护所有与provider的连接的channel
    @Autowired
    private ConnectionManager connectionManager;

    @Autowired
    private UnrespondedFutureHolder unrespondedFutureHolder;

    /**
     * afterInitialization:
     * <p>用于处理所有被@EurikaReference标注的域：构建其动态代理对象proxy，并使用proxy替代该域。</p>
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            EurikaReference annotationMetadata = field.getAnnotation(EurikaReference.class);
            if (null == annotationMetadata) {
                continue;
            }
            // 被@EurikaReference修饰的service，是connectionManager在注册中心关注的对象
            Class<?> fieldType = field.getType();
            connectionManager.addConnection(fieldType.getName(), annotationMetadata.group(), annotationMetadata.version());
            // 获得实现InvocationHandler接口的类proxy，其中定义了增强方法（对service的方法调用实际上是本代理类实现的）
            ReferencedServiceInvocationHandler proxy = new ReferencedServiceInvocationHandler(consumerClient, unrespondedFutureHolder, annotationMetadata);
            // 获得原始service类的代理类serviceProxy：与原始类的class类型一样，将对原始类的方法调用转交给proxy(3rd arg)实现
            Object serviceProxy = Proxy.newProxyInstance(fieldType.getClassLoader(), new Class[]{fieldType}, proxy);
            field.setAccessible(true);
            try {
                field.set(bean, serviceProxy);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return bean;
    }
}
