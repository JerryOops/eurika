package com.jerryoops.eurika.transmission.handler.shared;

import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.util.ApplicationContextUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import com.jerryoops.eurika.transmission.functioner.ServiceInvoker;
import com.jerryoops.eurika.transmission.listener.ExtraChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于处理RPC request，调用对应的方法并得到结果后，响应RPC response。
 */
@Slf4j
public class ProviderInvocationHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final ServiceInvoker serviceInvoker;

    private final int MAX_RETRY_TIMES = 3;

    public ProviderInvocationHandler() {
        this.serviceInvoker = ApplicationContextUtil.getBean(ServiceInvoker.class);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcRequest.checkValidity(request);
        RpcResponse<?> response = serviceInvoker.invoke(request);
        this.writeWithRetry(ctx, response);
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


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Exception caught: ", cause);
        if (cause instanceof EurikaException) {
            EurikaException e = (EurikaException) cause;
            Integer code = e.getCode();
            if (ResultCode.EXCEPTION_INVALID_PARAM.getCode().equals(code)) {
                // invalid param
                RpcResponse<?> response = RpcResponse.build((Long) e.getData(), ResultCode.EXCEPTION_INVALID_PARAM, e.getMsg(), null);
                ctx.pipeline().write(response);
            }
        }
    }


}
