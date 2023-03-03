package com.jerryoops.eurika.consumer.client.impl;

import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.consumer.client.ConsumerClient;
import com.jerryoops.eurika.consumer.functioner.ConnectionManager;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class NettyConsumerClient extends ConsumerClient {

    @Autowired
    private ConnectionManager connectionManager;

    // client引导类
    private Bootstrap bootstrap;

    /**
     * 启动客户端实例
     */
    @Override
    public void start() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // TODO: 2023/3/3 修改为配置设定
                ;
    }

    @Override
    public CompletableFuture<RpcResponse<?>> invokeRemote(RpcRequest request) {
        // TODO: 2023/3/3 to be changed
        log.info("NettyConsumerClient invokeRemote, request = {}", request);
        RpcResponse<String> response = RpcResponse.build(123L, ResultCode.OK, "OK", "Hello everyone!");
        return CompletableFuture.completedFuture(response);
    }
}
