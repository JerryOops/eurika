package com.jerryoops.eurika.transmission.handler.shared;

import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于处理RPC调用后得到的返回结果RPC response</p>
 */
@Slf4j
public class ConsumerInvocationHandler extends SimpleChannelInboundHandler<RpcResponse<?>> {

    ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.ctx = ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse<?> msg) throws Exception {
        // TODO: 2023/2/26 暂时未实现，此处简单将收到的response打印出来，后续会修改之
        log.info("Response = {}", msg);
    }

}
