package com.jerryoops.eurika.transmission.handler.http.initializer;

import com.jerryoops.eurika.transmission.handler.http.HttpMessageClientResolver;
import com.jerryoops.eurika.transmission.handler.shared.ConsumerInvocationHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

public class HttpConsumerChannelInitializer<C extends Channel> extends ChannelInitializer<C> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(
                new HttpClientCodec(),
                new HttpObjectAggregator(20 * 1024 * 1024),
                new HttpMessageClientResolver(),
                new ConsumerInvocationHandler()
        );
    }
}
