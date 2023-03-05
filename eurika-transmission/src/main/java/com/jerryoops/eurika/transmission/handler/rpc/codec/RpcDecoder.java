package com.jerryoops.eurika.transmission.handler.rpc.codec;

import com.jerryoops.eurika.common.constant.TransmissionConstant;
import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.CompressionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.enumeration.RpcMessageTypeEnum;
import com.jerryoops.eurika.common.enumeration.SerializationProtocolEnum;
import com.jerryoops.eurika.common.tool.compression.Compressor;
import com.jerryoops.eurika.common.tool.serialization.Serializer;
import com.jerryoops.eurika.transmission.domain.RpcMessage;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 将RpcMessage(未解码、离散)转变为RpcMessage(已解码的POJO实例)
 */
@Slf4j
public class RpcDecoder extends LengthFieldBasedFrameDecoder {

    public RpcDecoder() {
        this(TransmissionConstant.RPC_MESSAGE_MAX_LENGTH, 0, 4, -4, 0, true);
    }

    private RpcDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (null == decoded ||
                ((ByteBuf)decoded).readableBytes() < TransmissionConstant.RPC_MESSAGE_HEADER_LENGTH) {
            // 当此frame不足最小报文长度时，忽略之
            return decoded;
        }
        // 代表一个完整frame(header+body)的ByteBuf实例
        ByteBuf frameBuf = (ByteBuf) decoded;
        try {
            return this.doDecode(frameBuf);
        } finally {
            ReferenceCountUtil.release(frameBuf);
        }
    }

    /**
     * 将frameBuf转变为RpcMessage POJO实例
     * @param frameBuf 一个完整frame(header+body)的ByteBuf实例
     * @return
     */
    private RpcMessage doDecode(ByteBuf frameBuf) {
        // check parameter validity
        int length = frameBuf.readInt();
        byte magic = frameBuf.readByte();
        byte version = frameBuf.readByte();
        byte compression = frameBuf.readByte();
        byte serialization = frameBuf.readByte();
        byte type = frameBuf.readByte();
        frameBuf.readerIndex(frameBuf.readerIndex() + 7); // todo 跳过7个空的待扩展字节，用来存放clientId?
        long requestId = frameBuf.readLong();
        this.checkValidity(length, magic, version, compression, serialization, type);

        // build RpcMessage POJO instance
        RpcMessage rpcMessage = RpcMessage.builder().length(length).magic(magic).version(version)
                .compression(compression).serialization(serialization).type(type).requestId(requestId).build();
        int bodyLength = length - TransmissionConstant.RPC_MESSAGE_HEADER_LENGTH;
        if (bodyLength <= 0) {
            return rpcMessage;
        }
        byte[] bodyBytes = new byte[bodyLength];
        frameBuf.readBytes(bodyBytes);
        // decompress
        Compressor compressor = CompressionProtocolEnum.getCompressor(compression);
        bodyBytes = compressor.decompress(bodyBytes);
        Serializer serializer = SerializationProtocolEnum.getSerializer(serialization);
        Object body = null;
        // deserialize
        if (RpcMessageTypeEnum.RPC_REQUEST.equals(RpcMessageTypeEnum.get(type))) {
            body = serializer.deserialize(bodyBytes, RpcRequest.class);
        } else if (RpcMessageTypeEnum.RPC_RESPONSE.equals(RpcMessageTypeEnum.get(type))) {
            body = serializer.deserialize(bodyBytes, RpcResponse.class);
        }
        rpcMessage.setBody(body);
        log.info("rpcMessage = {}", rpcMessage);
        return rpcMessage;
    }

    /**
     * 检查RpcMessage报头参数的合法性。如有非法情况，则抛出异常。
     */
    private void checkValidity(int length, byte magic, byte version, byte compression, byte serialization, byte type) {
        String errorMessage = null;
        if (length < TransmissionConstant.RPC_MESSAGE_HEADER_LENGTH) {
            errorMessage = "Invalid length: " + length;
        } else if (magic != TransmissionConstant.RPC_MESSAGE_MAGIC) {
            errorMessage = "Invalid magic number: " + magic;
        } else if (version != TransmissionConstant.RPC_MESSAGE_VERSION) {
            errorMessage = "Invalid version: " + version;
        } else if (!CompressionProtocolEnum.isValid(compression)) {
            errorMessage = "Invalid compression code: " + compression;
        } else if (!SerializationProtocolEnum.isValid(serialization)) {
            errorMessage = "Invalid serialization code: " + serialization;
        } else if (!RpcMessageTypeEnum.isValid(type)) {
            errorMessage = "Invalid rpc message type: " + type;
        }
        if (null != errorMessage) {
            throw EurikaException.fail(ResultCode.EXCEPTION_INVALID_PARAM, errorMessage);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Exception caught: ", cause);
        if (cause instanceof EurikaException) {
            EurikaException e = (EurikaException) cause;
            if (ResultCode.EXCEPTION_INVALID_PARAM.getCode().equals(e.getCode())) {
                RpcResponse<Object> response = RpcResponse.build(null, ResultCode.EXCEPTION_INVALID_PARAM, e.getMsg(), null);
                ctx.pipeline().write(response);
            }
        }
    }


}
