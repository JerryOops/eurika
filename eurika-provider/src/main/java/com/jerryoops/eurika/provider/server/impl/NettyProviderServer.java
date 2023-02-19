package com.jerryoops.eurika.provider.server.impl;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.jerryoops.eurika.common.config.SpecifiedConfig;
import com.jerryoops.eurika.common.constant.ProviderConstant;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.util.StringEscapeUtil;
import com.jerryoops.eurika.provider.functioner.ServiceDeregistar;
import com.jerryoops.eurika.provider.functioner.ServiceHolder;
import com.jerryoops.eurika.provider.server.ProviderServer;
import com.jerryoops.eurika.registry.register.RegistryService;
import com.jerryoops.eurika.transmission.handler.ChannelHandlerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static com.jerryoops.eurika.common.constant.ProviderConstant.SERVICE_MAP_KEY_SEPARATOR;
import static com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum.HTTP;


@Slf4j
@Component
public class NettyProviderServer implements ProviderServer {

    @Autowired
    SpecifiedConfig specifiedConfig;
    @Autowired
    ServiceHolder serviceHolder;
    @Autowired
    ServiceDeregistar serviceDeregistar;
    @Autowired
    RegistryService registryService;

    private int port;
    private String host;

    private void initHostAndPort() {
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

    @Override
    public void start() {
        // 初始化本地IP地址(host)及可用的本地端口(port)
        this.initHostAndPort();
        // 将本实例中所有被@EurikaService标注的类注册到注册中心
        this.doRegister();
        // 添加shutdown hook
        serviceDeregistar.addShutdownHook();
        // 初始化provider server
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.getProcessorCount());
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .localAddress(port)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(ChannelHandlerInitializer.forProvider(HTTP));
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
    private void doRegister() {
        List<ServiceInfo> serviceInfoList = this.convertToServiceInfoList(serviceHolder.getServiceMapKeys(), host, port);
        registryService.register(serviceInfoList);
    }

    private List<ServiceInfo> convertToServiceInfoList(List<String> serviceMapKeys, String host, int port) {
        List<ServiceInfo> serviceInfoList = new ArrayList<>(serviceMapKeys.size());
        for (String key : serviceHolder.getServiceMapKeys()) {
            String[] splitKey = key.split(String.valueOf(SERVICE_MAP_KEY_SEPARATOR));
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceName(StringEscapeUtil.unescape(splitKey[0]));
            serviceInfo.setGroup(StringEscapeUtil.unescape(splitKey[1]));
            serviceInfo.setVersion(StringEscapeUtil.unescape(splitKey[2]));
            serviceInfo.setHost(host);
            serviceInfo.setPort(port);
            serviceInfoList.add(serviceInfo);
        }
        return serviceInfoList;
    }
}
