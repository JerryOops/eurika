package com.jerryoops.eurika.transmission.handler.http;

import com.jerryoops.eurika.common.enumeration.CompressionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.SerializationProtocolEnum;
import com.jerryoops.eurika.common.tool.compression.Compressor;
import com.jerryoops.eurika.common.tool.serialization.Serializer;
import com.jerryoops.eurika.common.util.NettyUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.listener.ExtraChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 出站handler。用于将来源于consumer的RPC request存入http request message中。
 */
@Slf4j
public class HttpRequestInstiller extends ChannelOutboundHandlerAdapter {

    private final int MAX_RETRY_TIMES = 3;

    /**
     * msg: RpcRequest object
     * out: HttpRequest object that encapsulated bytes of msg
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            if (msg instanceof RpcRequest) {
                RpcRequest request = (RpcRequest) msg;
                // serialize
                Serializer serializer = SerializationProtocolEnum.PROTOSTUFF.getSerializer();
                byte[] bytes = serializer.serialize(request);
                // compress
                Compressor compressor = CompressionProtocolEnum.GZIP.getCompressor();
                bytes = compressor.compress(bytes);
                // build http request
                HttpRequest httpRequest = NettyUtil.buildHttpRequest(bytes, HttpMethod.POST);
                this.writeWithRetry(ctx, httpRequest);
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
