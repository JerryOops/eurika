package com.jerryoops.eurika.transmission.handler.rpc.initializer;

import com.jerryoops.eurika.transmission.handler.rpc.RpcRequestDistiller;
import com.jerryoops.eurika.transmission.handler.rpc.RpcResponseInstiller;
import com.jerryoops.eurika.transmission.handler.rpc.codec.RpcDecoder;
import com.jerryoops.eurika.transmission.handler.rpc.codec.RpcEncoder;
import com.jerryoops.eurika.transmission.handler.shared.ProviderInvocationHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class RpcProviderChannelInitializer<C extends Channel> extends ChannelInitializer<C> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(
                new RpcDecoder(),
                new RpcEncoder(),
                new RpcRequestDistiller(),
                new RpcResponseInstiller(),
                new ProviderInvocationHandler()
        );
    }
}
