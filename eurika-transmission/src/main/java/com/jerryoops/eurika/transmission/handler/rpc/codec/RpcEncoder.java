package com.jerryoops.eurika.transmission.handler.rpc.codec;

import com.jerryoops.eurika.common.constant.TransmissionConstant;
import com.jerryoops.eurika.common.enumeration.CompressionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.SerializationProtocolEnum;
import com.jerryoops.eurika.common.tool.compression.Compressor;
import com.jerryoops.eurika.common.tool.serialization.Serializer;
import com.jerryoops.eurika.transmission.domain.RpcMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 将RpcMessage(POJO实例)进行编码和发送
 */
@Slf4j
public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        try {
            if (null != msg) {
                this.doEncode(ctx, msg, out);
            }
        } catch (Exception e) {
            log.warn("Exception caught during encoding process: ", e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void doEncode(ChannelHandlerContext ctx, RpcMessage message, ByteBuf out) {
        // header
        out.writerIndex(4); // 跳过报头的length字段占用的4字节
        out.writeByte(message.getMagic());
        out.writeByte(message.getVersion());
        out.writeByte(message.getCompression());
        out.writeByte(message.getSerialization());
        out.writeByte(message.getType());
        // 跳过待用区域
        out.writerIndex(out.writerIndex() + 7);
        out.writeLong(message.getRequestId()); // 8 Bytes

        // body
        byte[] bodyBytes = this.serializeBody(message);
        if (bodyBytes.length != 0) {
            out.writeBytes(bodyBytes);
        }
        // write back the length of header+body
        int tailIndex = out.writerIndex();
        out.writerIndex(0);
        out.writeInt(TransmissionConstant.RPC_MESSAGE_HEADER_LENGTH + bodyBytes.length);
        out.writerIndex(tailIndex);
    }

    private byte[] serializeBody(RpcMessage message) {
        if (null == message.getBody()) {
            return new byte[0];
        }
        // serialize
        Serializer serializer = SerializationProtocolEnum.getSerializer(message.getSerialization());
        byte[] bodyBytes = serializer.serialize(message.getBody());
        // compress
        Compressor compressor = CompressionProtocolEnum.getCompressor(message.getCompression());
        bodyBytes = compressor.compress(bodyBytes);
        return bodyBytes;
    }
}
