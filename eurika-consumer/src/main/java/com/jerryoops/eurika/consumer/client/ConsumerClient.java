package com.jerryoops.eurika.consumer.client;

import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;

import java.util.concurrent.CompletableFuture;

public abstract class ConsumerClient {

    /**
     * 开放给外部的方法，进行RPC调用远程方法。
     * @param request
     * @return
     */
    public abstract CompletableFuture<RpcResponse<?>> invokeRemote(RpcRequest request);
}
