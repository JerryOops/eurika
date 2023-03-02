package com.jerryoops.eurika.consumer.client;

import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;

import java.util.concurrent.CompletableFuture;

public abstract class ConsumerClient {

    /**
     * 开放给外部的方法，进行RPC调用远程方法。
     * @param request RpcRequest实例
     * @return 一个承载了远程方法返回结果对象的CompletableFuture实例。
     */
    public abstract CompletableFuture<RpcResponse<?>> invokeRemote(RpcRequest request);
}
