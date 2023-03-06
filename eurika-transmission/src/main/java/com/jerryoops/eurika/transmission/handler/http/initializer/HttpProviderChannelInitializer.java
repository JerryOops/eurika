package com.jerryoops.eurika.transmission.handler.http.initializer;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.jerryoops.eurika.transmission.handler.http.HttpMessageServerResolver;
import com.jerryoops.eurika.transmission.handler.shared.ProviderInvocationHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class HttpProviderChannelInitializer<C extends Channel> extends ChannelInitializer<C> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        DefaultEventExecutorGroup eventExecutors = new DefaultEventExecutorGroup(
                RuntimeUtil.getProcessorCount() * 2,
                ThreadUtil.newNamedThreadFactory("provider-invocation-handler", false));
        ch.pipeline().addLast(
                new HttpServerCodec(),
                new HttpObjectAggregator(20 * 1024 * 1024),
                new HttpMessageServerResolver()
        ).addLast(
                eventExecutors, new ProviderInvocationHandler()
        );
    }
}
