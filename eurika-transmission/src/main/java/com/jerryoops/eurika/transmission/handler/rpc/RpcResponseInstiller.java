package com.jerryoops.eurika.transmission.handler.rpc;

import cn.hutool.core.lang.UUID;
import com.jerryoops.eurika.common.constant.TransmissionConstant;
import com.jerryoops.eurika.common.enumeration.CompressionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.RpcMessageTypeEnum;
import com.jerryoops.eurika.common.enumeration.SerializationProtocolEnum;
import com.jerryoops.eurika.transmission.domain.RpcMessage;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

/**
 * 构建一个RpcMessage对象，将传入的RpcResponse对象作为rpcMessage.body，并将rpcMessage传递给下一个handler。
 */
public class RpcResponseInstiller extends ChannelOutboundHandlerAdapter {

    /**
     * msg: RpcResponse
     * out: RpcMessage, whose field body is instilled with msg
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            if (msg instanceof RpcResponse) {
                RpcResponse<?> response = (RpcResponse<?>) msg;
                // length not filled
                RpcMessage message = RpcMessage.builder()
                        .magic(TransmissionConstant.RPC_MESSAGE_MAGIC)
                        .version(TransmissionConstant.RPC_MESSAGE_VERSION)
                        .compression(CompressionProtocolEnum.GZIP.getCode()) // TODO: 2023/2/26 使用配置文件来决定
                        .serialization(SerializationProtocolEnum.PROTOSTUFF.getCode())
                        .type(RpcMessageTypeEnum.RPC_RESPONSE.getCode())
                        .requestId(response.getRequestId())
                        .body(response)
                        .build();
                ctx.writeAndFlush(message, promise); // TODO: 2023/2/26 retry
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
