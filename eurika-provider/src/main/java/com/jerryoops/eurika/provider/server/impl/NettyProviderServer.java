package com.jerryoops.eurika.provider.server.impl;

import cn.hutool.core.net.NetUtil;
import com.jerryoops.eurika.common.config.SpecifiedConfig;
import com.jerryoops.eurika.common.constant.ProviderConstant;
import com.jerryoops.eurika.common.domain.ServiceAnnotationInfo;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.provider.holder.ServiceHolder;
import com.jerryoops.eurika.provider.server.ProviderServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class NettyProviderServer implements ProviderServer {

    @Autowired
    SpecifiedConfig specifiedConfig;
    @Autowired
    ServiceHolder serviceHolder;

    private int port;
    private String host;

    private void initHostAndPort() throws UnknownHostException {
        port = ProviderConstant.DEFAULT_PORT;
        if (!NetUtil.isUsableLocalPort(port)) {
            port = NetUtil.getUsableLocalPort(port);
        }
        host = InetAddress.getLocalHost().getHostAddress();
    }

    @Override
    public void start() {
        // 初始化本地IP地址(host)及可用的本地端口(port)
        try {
            this.initHostAndPort();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        // 将本实例中所有被@EurikaService标注的类注册到注册中心

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .localAddress(port)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // TODO: add handlers
                        }
                    });
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


    /**
     * 调用RegistryService.register方法，将所有被@EurikaService标注的类的服务信息注册到服务注册中心。
     */
}
