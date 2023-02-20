package com.jerryoops.eurika.transmission.handler.http;

import com.jerryoops.eurika.common.util.JsonUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 入站handler。用于将来源于consumer的http request message中的RPC request提取出来。
 */
@Slf4j
public class HttpRequestDistiller extends ChannelInboundHandlerAdapter {

    /**
     * msg: FullHttpRequest object
     * out: RpcRequest object, distilled from HTTP POST request body
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FullHttpRequest) {
                String json = ((FullHttpRequest) msg).content().toString(StandardCharsets.UTF_8);
                RpcRequest rpcRequest = JsonUtil.fromJson(json, RpcRequest.class);
                ctx.fireChannelRead(rpcRequest);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
