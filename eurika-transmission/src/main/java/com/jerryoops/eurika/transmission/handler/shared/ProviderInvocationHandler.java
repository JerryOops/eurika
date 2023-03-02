package com.jerryoops.eurika.transmission.handler.shared;

import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.util.ApplicationContextUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import com.jerryoops.eurika.transmission.functioner.ServiceInvoker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于处理RPC request，调用对应的方法并得到结果后，响应RPC response。
 */
@Slf4j
public class ProviderInvocationHandler extends SimpleChannelInboundHandler<RpcRequest> {
    ServiceInvoker serviceInvoker;

    public ProviderInvocationHandler() {
        this.serviceInvoker = ApplicationContextUtil.getBean(ServiceInvoker.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcRequest.checkValidity(request);
        RpcResponse<?> response = serviceInvoker.invoke(request);
        ctx.pipeline().write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Exception caught: ", cause);
        if (cause instanceof EurikaException) {
            EurikaException e = (EurikaException) cause;
            Integer code = e.getCode();
            if (ResultCode.EXCEPTION_INVALID_PARAM.getCode().equals(code)) {
                RpcResponse<?> response = RpcResponse.build((Long) e.getData(), ResultCode.EXCEPTION_INVALID_PARAM, e.getMsg(), null);
                ctx.pipeline().write(response);
            }
        }
    }


}
