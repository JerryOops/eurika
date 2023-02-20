package com.jerryoops.eurika.transmission.handler.shared;

import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.util.ApplicationContextUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import com.jerryoops.eurika.transmission.functioner.ServiceInvoker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 用于处理RPC request，调用对应的方法并得到结果后，响应RPC response
 */
public class ProviderMessageHandler extends SimpleChannelInboundHandler<RpcRequest> {
    ServiceInvoker serviceInvoker;

    public ProviderMessageHandler() {
        this.serviceInvoker = ApplicationContextUtil.getBean(ServiceInvoker.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcRequest.checkValidity(request);
        RpcResponse response = serviceInvoker.invoke(request);
        ctx.writeAndFlush(response); // TODO: 2023/2/20 retry
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof EurikaException) {
            EurikaException e = (EurikaException) cause;
            Integer code = e.getCode();
            if (ResultCode.EXCEPTION_INVALID_PARAM.getCode().equals(code)) {
                String requestId = (String) e.getData();
                RpcResponse response = RpcResponse.builder()
                        .requestId(requestId)
                        .code(ResultCode.EXCEPTION_INVALID_PARAM.getCode())
                        .msg(e.getMsg()).build();
                ctx.writeAndFlush(response); // TODO: 2023/2/20 retry
            }
        }
    }


}
