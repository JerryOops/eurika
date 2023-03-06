package com.jerryoops.eurika.transmission.handler.http;

import com.google.gson.JsonSyntaxException;
import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.CompressionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.enumeration.SerializationProtocolEnum;
import com.jerryoops.eurika.common.tool.compression.Compressor;
import com.jerryoops.eurika.common.tool.serialization.Serializer;
import com.jerryoops.eurika.common.util.JsonUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * <p>provider's inbound handler。用于将来源于consumer的http request message content中的RPC request提取出来。</p>
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
                ByteBuf buf = ((FullHttpRequest) msg).content();
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
                RpcRequest request = serializer.deserialize(bytes, RpcRequest.class);
                ctx.fireChannelRead(request);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
