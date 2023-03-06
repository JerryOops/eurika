package com.jerryoops.eurika.consumer.client.impl;

import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum;
import com.jerryoops.eurika.consumer.client.ConsumerClient;
import com.jerryoops.eurika.consumer.functioner.ConnectionManager;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import com.jerryoops.eurika.transmission.functioner.UnrespondedFutureHolder;
import com.jerryoops.eurika.transmission.handler.ChannelHandlerInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
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
    @Autowired
    private UnrespondedFutureHolder unrespondedFutureHolder;

    // client引导类
    private final Bootstrap bootstrap;

    {
        bootstrap = new Bootstrap();
    }

    /**
     * 启动客户端实例
     */
    @Override
    public void start() {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // TODO: 2023/3/3 修改为配置设定
                .handler(ChannelHandlerInitializer.forConsumer(TransmissionProtocolEnum.HTTP));
    }


    /**
     * 开放给外部的方法，进行RPC调用远程方法：向channel的outbound方向写RpcRequest。
     * @param request RpcRequest实例
     * @return 一个承载了远程方法返回结果对象的CompletableFuture实例。
     */
    @Override
    public CompletableFuture<RpcResponse<?>> invokeRemote(RpcRequest request) {
        // 将completableFuture放入unrespondedFutureHolder中
        CompletableFuture<RpcResponse<?>> completableFuture = new CompletableFuture<>();
        unrespondedFutureHolder.put(request.getRequestId(), completableFuture);

        // 获取与目标provider的连接(一个channel实例)
        Channel channel = connectionManager.getChannel(this.bootstrap, request);
        if (null == channel || !channel.isActive()) {
            unrespondedFutureHolder.completeExceptionally(request.getRequestId(),
                    EurikaException.fail(ResultCode.EXCEPTION_CHANNEL_UNAVAILABLE, "Channel unavailable"));
            return completableFuture;
        }
        // 发送RpcRequest消息
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.info("RpcRequest successfully sent by client: {}", request);
                } else {
                    // TODO: 2023/3/4 retry?
                    log.info("RpcRequest sending failed", future.cause());
                    unrespondedFutureHolder.completeExceptionally(request.getRequestId(), future.cause());
                }
            }
        });
        return completableFuture;
    }
}
