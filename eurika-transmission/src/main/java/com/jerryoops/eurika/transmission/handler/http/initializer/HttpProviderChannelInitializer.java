package com.jerryoops.eurika.transmission.handler.http.initializer;

import com.jerryoops.eurika.transmission.handler.http.HttpRequestDistiller;
import com.jerryoops.eurika.transmission.handler.http.HttpResponseBuilder;
import com.jerryoops.eurika.transmission.handler.shared.ProviderMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpProviderChannelInitializer<C extends Channel> extends ChannelInitializer<C> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(
                new HttpServerCodec(),
                new HttpObjectAggregator(10 * 1024 * 1024),
                new HttpRequestDistiller(),
                new HttpResponseBuilder(),
                new ProviderMessageHandler()
        );
    }
}
