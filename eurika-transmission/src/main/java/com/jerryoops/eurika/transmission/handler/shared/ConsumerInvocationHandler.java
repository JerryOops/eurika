package com.jerryoops.eurika.transmission.handler.shared;

import com.jerryoops.eurika.common.util.ApplicationContextUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import com.jerryoops.eurika.transmission.functioner.UnrespondedFutureHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于处理RPC调用后得到的返回结果RPC response</p>
 */
@Slf4j
public class ConsumerInvocationHandler extends SimpleChannelInboundHandler<RpcResponse<?>> {

    private final UnrespondedFutureHolder unrespondedFutureHolder;

    public ConsumerInvocationHandler() {
        this.unrespondedFutureHolder = ApplicationContextUtil.getBean(UnrespondedFutureHolder.class);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse<?> msg) throws Exception {
        unrespondedFutureHolder.complete(msg);
    }

}
