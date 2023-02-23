package com.jerryoops.eurika.transmission.handler.http;

import com.google.gson.JsonSyntaxException;
import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.util.JsonUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * <p>入站handler。用于将来源于consumer的http request message中的RPC request提取出来。</p>
 */
@Slf4j
public class HttpRequestDistiller extends ChannelInboundHandlerAdapter {

    /**
     * msg: FullHttpRequest object
     * out: RpcRequest object, distilled from HTTP POST request body
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String json = null;
        try {
            if (msg instanceof FullHttpRequest) {
                json = ((FullHttpRequest) msg).content().toString(StandardCharsets.UTF_8);
                if (StringUtils.isBlank(json)) {
                    return;
                }
                RpcRequest rpcRequest = RpcRequest.parseJson(json);
                ctx.fireChannelRead(rpcRequest);
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            log.warn("Exception caught: ", e);
            // Json syntax error
            RpcResponse<Object> response = RpcResponse.build(null, ResultCode.EXCEPTION_INVALID_PARAM, "Json syntax error: " + json, null);
            ctx.pipeline().write(response);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Exception caught: ", cause);
         if (cause instanceof EurikaException) {
            EurikaException e = (EurikaException) cause;
            Integer code = e.getCode();
            if (ResultCode.EXCEPTION_CLASS_NOT_FOUND.getCode().equals(code)) {
                // RpcRequest.parseJson(json)中抛出的异常，parameterTypes中某个元素无法从json转为class bean（CLASS NOT FOUND）
                RpcResponse<Object> response = RpcResponse.build((String) e.getData(), ResultCode.EXCEPTION_CLASS_NOT_FOUND, e.getMsg(), null);
                ctx.pipeline().write(response);
            } else if (ResultCode.EXCEPTION_INVALID_PARAM.getCode().equals(code)) {
                // RpcRequest.parseJson(json)中抛出的异常，parameterTypes与parameters两者或之一不存在；或者二者的长度不一致
                RpcResponse<Object> response = RpcResponse.build((String) e.getData(), ResultCode.EXCEPTION_INVALID_PARAM, e.getMsg(), null);
                ctx.pipeline().write(response);
            }
        }
    }
}
