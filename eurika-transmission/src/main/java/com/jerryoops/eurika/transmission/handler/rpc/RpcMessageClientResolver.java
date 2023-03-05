package com.jerryoops.eurika.transmission.handler.rpc;

import io.netty.channel.CombinedChannelDuplexHandler;

public class RpcMessageClientResolver extends CombinedChannelDuplexHandler<RpcResponseDistiller, RpcRequestInstiller> {

    public RpcMessageClientResolver() {
        super();
        super.init(new RpcResponseDistiller(), new RpcRequestInstiller());
    }
}
