package com.jerryoops.eurika.transmission.handler.rpc;

import com.jerryoops.eurika.transmission.domain.RpcMessage;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 将传入的RpcMessage对象中的body域实例提取出来。如果是RpcResponse，则将其传递给下一个handler。
 */
@Slf4j
public class RpcResponseDistiller extends ChannelInboundHandlerAdapter {

    /**
     * msg: RpcMessage
     * out: RpcResponse, distilled from field body of msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof RpcMessage) {
                RpcMessage message = (RpcMessage) msg;
                Object body = message.getBody();
                if (body instanceof RpcResponse) {
                    ctx.fireChannelRead(body);
                } else {
                    log.warn("body is not an instance of RpcResponse (class = {})", body.getClass());
                    // stop the message here & do not pass it to next inbound handler
                }
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
