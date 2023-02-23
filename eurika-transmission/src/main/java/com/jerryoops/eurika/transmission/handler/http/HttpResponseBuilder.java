package com.jerryoops.eurika.transmission.handler.http;

import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.util.JsonUtil;
import com.jerryoops.eurika.common.util.NettyUtil;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 出站handler。用于将来源于provider的RPC response存入http response message中。
 */
@Slf4j
public class HttpResponseBuilder extends ChannelOutboundHandlerAdapter {

    /**
     * msg: RpcResponse object
     * out: HttpResponse object that encapsulated json string of RpcResponse
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            if (msg instanceof RpcResponse) {
                RpcResponse<?> response = (RpcResponse<?>) msg;
                String json = JsonUtil.toJson(response);
                HttpResponse httpResponse = NettyUtil.buildHttpResponse(json, ResultCode.mapHttp(response.getCode()));
                ctx.writeAndFlush(httpResponse, promise);
            }
        } catch (Exception e) {
            log.info("Exception caught: ", e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }



}
