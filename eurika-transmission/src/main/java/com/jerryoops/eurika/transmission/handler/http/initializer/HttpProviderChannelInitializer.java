package com.jerryoops.eurika.transmission.handler.http.initializer;

import com.jerryoops.eurika.transmission.handler.http.HttpMessageServerResolver;
import com.jerryoops.eurika.transmission.handler.shared.ProviderInvocationHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpProviderChannelInitializer<C extends Channel> extends ChannelInitializer<C> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(
                new HttpServerCodec(),
                new HttpObjectAggregator(20 * 1024 * 1024),
                new HttpMessageServerResolver(),
                new ProviderInvocationHandler()
        );
    }
}
