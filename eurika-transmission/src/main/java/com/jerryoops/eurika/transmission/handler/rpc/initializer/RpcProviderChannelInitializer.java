package com.jerryoops.eurika.transmission.handler.rpc.initializer;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RuntimeUtil;
import com.jerryoops.eurika.transmission.handler.rpc.RpcMessageServerResolver;
import com.jerryoops.eurika.transmission.handler.rpc.RpcRequestDistiller;
import com.jerryoops.eurika.transmission.handler.rpc.RpcResponseInstiller;
import com.jerryoops.eurika.transmission.handler.rpc.codec.RpcDecoder;
import com.jerryoops.eurika.transmission.handler.rpc.codec.RpcEncoder;
import com.jerryoops.eurika.transmission.handler.shared.ProviderInvocationHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class RpcProviderChannelInitializer<C extends Channel> extends ChannelInitializer<C> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        DefaultEventExecutorGroup eventExecutors = new DefaultEventExecutorGroup(
                RuntimeUtil.getProcessorCount() * 2,
                ThreadUtil.newNamedThreadFactory("provider-invocation-handler", false));
        ch.pipeline().addLast(
                new RpcDecoder(),
                new RpcEncoder(),
                new RpcMessageServerResolver()
        ).addLast(
                eventExecutors, new ProviderInvocationHandler()
        );
    }
}
