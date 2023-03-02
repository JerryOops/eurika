package com.jerryoops.eurika.common.domain.listener;

/**
 * 开放给用户使用的CallbackListener接口。
 * <p>使用场景：当用户选择使用异步的方式进行RPC调用时（即在@EurikaReference中设置了async=true），
 * 则需要同时向@EurikaReference的listeners属性中添加用户继承此接口的实现类。</p>
 * <p>注意事项：用户在实现本接口时需要确保实现类具有无参构造器。</p>
 */
public interface CallbackListener {

    /**
     * 当异步RPC调用结束时被调用。对应CompletableFuture.whenCompleteAsync()中的result。
     * @param result
     */
    void onExecutionCompleted(Object result);

    /**
     * 当异步RPC调用发生异常时。对应对应CompletableFuture.whenCompleteAsync()中的throwable。
     * @param th
     */
    void onThrowableCaught(Throwable th);
}
