package com.jerryoops.eurika.test.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestHttpServerChannelInitializer extends ChannelInitializer<Channel> {

    @Autowired
    MethodCallerInboundHandler methodCallerInboundHandler;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline()
                .addLast("codec", new HttpServerCodec())
                .addLast("aggregator", new HttpObjectAggregator(512 * 1024))
                .addLast("echo", methodCallerInboundHandler);
    }
}
