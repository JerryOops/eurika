package com.jerryoops.eurika.consumer.proxy;

import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.spring.annotation.EurikaReference;
import com.jerryoops.eurika.common.tool.config.ConfigManager;
import com.jerryoops.eurika.common.tool.id.IdGenerator;
import com.jerryoops.eurika.consumer.client.ConsumerClient;
import com.jerryoops.eurika.common.domain.listener.callback.CallbackListener;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import com.jerryoops.eurika.transmission.functioner.UnrespondedFutureHolder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 被@EurikaReference标注的service是被代理者，本类是其代理者。
 * <p>在调用方的代码中，当一个service域被@EurikaReference标注时，对这个service的方法调用实际上是本代理类实现的。</p>
 */
@Slf4j
public class ReferencedServiceInvocationHandler implements InvocationHandler {

    // RPC调用中，消费者一端的、负责发送和接收RPC信息的client实例
    private final ConsumerClient consumerClient;
    // 未响应的CompletableFuture实例的持有者
    private final UnrespondedFutureHolder unrespondedFutureHolder;
    // 与被代理类(被@EurikaReference修饰的类实例)绑定的EurikaReference信息
    private final EurikaReference annotationMetadata;



    // constructor
    public ReferencedServiceInvocationHandler(ConsumerClient consumerClient, UnrespondedFutureHolder unrespondedFutureHolder, EurikaReference annotationMetadata) {
        this.consumerClient = consumerClient;
        this.annotationMetadata = annotationMetadata;
        this.unrespondedFutureHolder = unrespondedFutureHolder;
    }

    /**
     * 被代理者的方法被调用时，本方法被唤起。
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = this.buildRpcRequest(method, args);
        // invoke remote method using consumerClient
        CompletableFuture<RpcResponse<?>> future = consumerClient.invokeRemote(request);
        // sync call
        if (!annotationMetadata.async()) {
            Integer timeoutInvocation = ConfigManager.getConsumerConfig().getTimeoutInvocation();
            try {
                RpcResponse<?> response = future.get(timeoutInvocation, TimeUnit.MILLISECONDS);
                // TODO: 2023/3/2 check response's validity and if any exception occurred on provider-side
                return response.getResult();

            } catch (Exception e) {
                if (e instanceof TimeoutException) {
                    log.error("Synchronous RPC invocation exceeded maximum waiting time of {}!", timeoutInvocation, e);
                } else if (e instanceof ExecutionException) {
                    String wrappedMessage = (e.getCause() instanceof EurikaException) ? ((EurikaException)e.getCause()).getMsg() : e.getCause().getMessage();
                    log.error("Future completed exceptionally, wrapped message is [{}]", wrappedMessage, e);
                } else {
                    log.error("Exception caught: ", e);
                }
                // 再次尝试从unrespondedFutureHolder中移除该future。如果已移除，则不做任何事情。
                unrespondedFutureHolder.completeExceptionally(request.getRequestId(), e);
            }
            return null;
        }

        // async call
        CallbackListener listener = this.buildCallbackListener();
        if (null == listener) {
            return null;
        }
        future.whenCompleteAsync(
                (response, throwable) -> {
                    listener.onExecutionCompleted(response.getResult());
                    if (null != throwable) {
                        listener.onThrowableCaught(throwable);
                    }
                }
        );
        return null;
    }

    /**
     * 构建RpcRequest实例。
     */
    private RpcRequest buildRpcRequest(Method method, Object[] args) {
        // build RpcRequest instance
        RpcRequest request = new RpcRequest();
        request.setRequestId(IdGenerator.generateRequestId());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(annotationMetadata.version());
        request.setGroup(annotationMetadata.group());
        return request;
    }


    /**
     * 构建@EurikaReference中listener属性指定的回调监听器实例。
     * @return 一个用户自定义的、实现了CallbackListener接口的回调监听器实例。
     */
    private CallbackListener buildCallbackListener() {
        // build CallbackListener instance using reflection
        Class<?> listenerClazz = annotationMetadata.listener();
        if (Void.class.equals(listenerClazz) || !(CallbackListener.class.isAssignableFrom(listenerClazz))) {
            log.warn("Invoking remote method in async mode without specifying CallbackListener in @EurikaReference," +
                    "listenerClazz = {}", listenerClazz);
            return null;
        }
        try {
            // No-arg constructor
            Constructor<?> constructor = listenerClazz.getConstructor();
            constructor.setAccessible(true);
            return (CallbackListener) constructor.newInstance();
        } catch (NoSuchMethodException e) {
            log.error("No-arg constructor does not exist in {}, " +
                    "WILL NOT be able to get result or execute callback method", listenerClazz, e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            log.error("Error occurred during instantiating {}," +
                    "WILL NOT be able to get result or execute callback method", listenerClazz, e);
        }
        return null;
    }
}
