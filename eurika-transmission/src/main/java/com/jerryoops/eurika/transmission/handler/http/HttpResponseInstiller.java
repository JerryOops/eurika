package com.jerryoops.eurika.transmission.handler.http;

import com.jerryoops.eurika.common.enumeration.CompressionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.enumeration.SerializationProtocolEnum;
import com.jerryoops.eurika.common.tool.compression.Compressor;
import com.jerryoops.eurika.common.tool.serialization.Serializer;
import com.jerryoops.eurika.common.util.JsonUtil;
import com.jerryoops.eurika.common.util.NettyUtil;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import com.jerryoops.eurika.transmission.listener.ExtraChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 出站handler。用于将来源于provider的RPC response存入http response message中。
 */
@Slf4j
public class HttpResponseInstiller extends ChannelOutboundHandlerAdapter {

    private final int MAX_RETRY_TIMES = 3;

    /**
     * msg: RpcResponse object
     * out: HttpResponse object that encapsulated bytes of msg
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            if (msg instanceof RpcResponse) {
                RpcResponse<?> response = (RpcResponse<?>) msg;
                // serialize
                Serializer serializer = SerializationProtocolEnum.PROTOSTUFF.getSerializer();
                byte[] bytes = serializer.serialize(response);
                // compress
                Compressor compressor = CompressionProtocolEnum.GZIP.getCompressor();
                bytes = compressor.compress(bytes);
                // build http response
                HttpResponse httpResponse = NettyUtil.buildHttpResponse(bytes, ResultCode.mapHttp(response.getCode()));
                this.writeWithRetry(ctx, httpResponse);
            }
        } catch (Exception e) {
            log.warn("Exception caught: ", e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void writeWithRetry(ChannelHandlerContext ctx, Object message) {
        this.retry(ctx, message,  0);
    }

    private void retry(ChannelHandlerContext ctx, Object message, int retriedTimes) {
        ctx.writeAndFlush(message).addListener(
                ExtraChannelFutureListener.retryListener(
                        () -> this.retry(ctx, message, retriedTimes + 1),
                        retriedTimes,
                        MAX_RETRY_TIMES,
                        5000
                )
        );
    }



}
