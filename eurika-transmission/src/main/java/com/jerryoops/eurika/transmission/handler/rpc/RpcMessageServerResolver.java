package com.jerryoops.eurika.transmission.handler.rpc;

import io.netty.channel.CombinedChannelDuplexHandler;

public class RpcMessageServerResolver extends CombinedChannelDuplexHandler<RpcRequestDistiller, RpcResponseInstiller> {

    public RpcMessageServerResolver() {
        super();
        super.init(new RpcRequestDistiller(), new RpcResponseInstiller());
    }
}
