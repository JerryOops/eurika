package com.jerryoops.eurika.transmission.handler.rpc;

import com.jerryoops.eurika.common.constant.TransmissionConstant;
import com.jerryoops.eurika.common.enumeration.CompressionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.RpcMessageTypeEnum;
import com.jerryoops.eurika.common.enumeration.SerializationProtocolEnum;
import com.jerryoops.eurika.transmission.domain.RpcMessage;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

/**
 * 将准备要发送的RpcRequest注入RpcMessage中
 */
public class RpcRequestInstiller extends ChannelOutboundHandlerAdapter {

    /**
     * msg: RpcRequest
     * out: RpcMessage, whose field body is instilled with msg
     */
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            if (msg instanceof RpcRequest) {
                RpcRequest request = (RpcRequest) msg;
                // length not filled
                RpcMessage message = RpcMessage.builder()
                        .magic(TransmissionConstant.RPC_MESSAGE_MAGIC)
                        .version(TransmissionConstant.RPC_MESSAGE_VERSION)
                        .compression(CompressionProtocolEnum.GZIP.getCode())
                        .serialization(SerializationProtocolEnum.PROTOSTUFF.getCode())
                        .type(RpcMessageTypeEnum.RPC_REQUEST.getCode())
                        .requestId(request.getRequestId())
                        .body(request)
                        .build();
                ctx.writeAndFlush(message, promise); // TODO: 2023/3/4 retry?
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
