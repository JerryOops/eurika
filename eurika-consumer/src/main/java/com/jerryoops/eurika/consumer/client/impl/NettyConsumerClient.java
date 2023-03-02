package com.jerryoops.eurika.consumer.client.impl;

import com.jerryoops.eurika.consumer.client.ConsumerClient;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;

import java.util.concurrent.CompletableFuture;

public class NettyConsumerClient extends ConsumerClient {


    @Override
    public CompletableFuture<RpcResponse<?>> invokeRemote(RpcRequest request) {
        return null;
    }
}
