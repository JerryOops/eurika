package com.jerryoops.eurika.consumer.client.impl;

import com.jerryoops.eurika.consumer.client.ConsumerClient;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class NettyConsumerClient extends ConsumerClient {


    @Override
    public CompletableFuture<RpcResponse<?>> invokeRemote(RpcRequest request) {
        return null;
    }
}
