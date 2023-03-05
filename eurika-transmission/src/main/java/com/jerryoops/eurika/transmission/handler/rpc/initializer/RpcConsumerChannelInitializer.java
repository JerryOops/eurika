package com.jerryoops.eurika.transmission.handler.rpc.initializer;

import com.jerryoops.eurika.transmission.handler.rpc.RpcMessageClientResolver;
import com.jerryoops.eurika.transmission.handler.rpc.RpcRequestInstiller;
import com.jerryoops.eurika.transmission.handler.rpc.RpcResponseDistiller;
import com.jerryoops.eurika.transmission.handler.rpc.codec.RpcDecoder;
import com.jerryoops.eurika.transmission.handler.rpc.codec.RpcEncoder;
import com.jerryoops.eurika.transmission.handler.shared.ConsumerInvocationHandler;
import com.jerryoops.eurika.transmission.handler.shared.ProviderInvocationHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class RpcConsumerChannelInitializer<C extends Channel> extends ChannelInitializer<C> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(
                new RpcEncoder(),
                new RpcDecoder(),
                new RpcMessageClientResolver(),
                new ConsumerInvocationHandler()
        );
    }
}
