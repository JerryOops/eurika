package com.jerryoops.eurika.consumer.client.impl;

import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum;
import com.jerryoops.eurika.consumer.client.ConsumerClient;
import com.jerryoops.eurika.consumer.functioner.ConnectionManager;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import com.jerryoops.eurika.transmission.handler.ChannelHandlerInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
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
                .handler(ChannelHandlerInitializer.forConsumer(TransmissionProtocolEnum.RPC));
    }


    /**
     * 开放给外部的方法，进行RPC调用远程方法。
     * @param request RpcRequest实例
     * @return 一个承载了远程方法返回结果对象的CompletableFuture实例。
     */
    @Override
    public CompletableFuture<RpcResponse<?>> invokeRemote(RpcRequest request) {
        // 获取与目标provider的连接(一个已连接成功的channel实例)
        Channel channel = connectionManager.getChannel(request.getClassName(), request.getGroup(), request.getVersion());
        // 构建RpcRequest消息并发送
        return null;
    }
}
