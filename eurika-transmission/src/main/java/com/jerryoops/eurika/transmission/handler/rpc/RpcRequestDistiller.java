package com.jerryoops.eurika.transmission.handler.rpc;

import com.jerryoops.eurika.transmission.domain.RpcMessage;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 将传入的RpcMessage对象中的body域实例提取出来。如果是RpcRequest，则将其传递给下一个handler。
 */
@Slf4j
public class RpcRequestDistiller extends ChannelInboundHandlerAdapter {

    /**
     * msg: RpcMessage
     * out: RpcRequest, distilled from field(body) of msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                RpcMessage message = (RpcMessage) msg;
                Object body = message.getBody();
                if (!(body instanceof RpcRequest)) {
                    // TODO: 2023/2/24 throw exception
                    log.warn("body is not instance of RpcRequest ... body.class = {}", body.getClass());
                }
                ctx.fireChannelRead(body);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Exception caught: ", cause);
    }
}
