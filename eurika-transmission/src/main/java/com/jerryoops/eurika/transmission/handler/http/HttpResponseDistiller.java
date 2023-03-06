package com.jerryoops.eurika.transmission.handler.http;

import com.jerryoops.eurika.common.enumeration.CompressionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.SerializationProtocolEnum;
import com.jerryoops.eurika.common.tool.compression.Compressor;
import com.jerryoops.eurika.common.tool.serialization.Serializer;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;

/**
 * <p>consumer's inbound handler。用于将来源于provider的http response message content中的RPC response提取出来。</p>
 */
public class HttpResponseDistiller extends ChannelInboundHandlerAdapter {

    /**
     * msg: FullHttpResponse object
     * out: RpcResponse object, distilled from HTTP response body
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof FullHttpResponse) {
                ByteBuf buf = ((FullHttpResponse) msg).content();
                if (buf.readableBytes() <= 0) {
                    return;
                }
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                // decompress
                Compressor compressor = CompressionProtocolEnum.GZIP.getCompressor();
                bytes = compressor.decompress(bytes);
                // deserialize
                Serializer serializer = SerializationProtocolEnum.PROTOSTUFF.getSerializer();
                RpcResponse<?> response = serializer.deserialize(bytes, RpcResponse.class);
                ctx.fireChannelRead(response);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
