package com.jerryoops.eurika.provider.server.impl;

import cn.hutool.core.net.NetUtil;
import com.jerryoops.eurika.common.constant.ProviderConstant;
import com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum;
import com.jerryoops.eurika.common.tool.config.ConfigManager;
import com.jerryoops.eurika.provider.server.ProviderServer;
import com.jerryoops.eurika.transmission.handler.ChannelHandlerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;


@Slf4j
@Component
public class NettyProviderServer extends ProviderServer {

    /**
     * 初始化本地IP地址(host)及可用的本地端口(port)，然后启动RPC服务器实例。
     */
    @PostConstruct
    private void start() {
        this.initHostPort();
        Executors.newSingleThreadExecutor().submit(this::startServer);
    }

    private void initHostPort() {
        port = ProviderConstant.DEFAULT_PORT;
        if (!NetUtil.isUsableLocalPort(port)) {
            port = NetUtil.getUsableLocalPort(port);
        }
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private void startServer() {
        // 启动provider server
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        String protocolName = ConfigManager.getProviderConfig().getProtocol();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .localAddress(port)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(ChannelHandlerInitializer.forProvider(
                            TransmissionProtocolEnum.getByName(protocolName)));
            ChannelFuture bindFuture = b.bind().sync();
            log.info("NettyProviderServer successfully started on port {}", port);
            // 监听等待channel关闭
            bindFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.warn("Exception caught when starting NettyProviderServer", e);
        } finally {
            log.warn("NettyProviderServer shutdown started");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
