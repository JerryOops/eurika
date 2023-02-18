package com.jerryoops.eurika.test.server;

import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.util.JsonUtil;
import com.jerryoops.eurika.registry.register.RegistryService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;

@Slf4j
@Component
public class MethodCallerInboundHandler extends ChannelInboundHandlerAdapter {

    /**
     * thread-private object
     */
    private HttpRequest httpRequest;

    @Autowired
    private RegistryService registryService;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            this.httpRequest = (HttpRequest) msg;
        }
        if (msg instanceof HttpContent) {
            HttpContent httpContent = (HttpContent) msg;
            ByteBuf buf = httpContent.content();
            this.invokeMethod(buf.toString(StandardCharsets.UTF_8));
            ctx.writeAndFlush(this.buildResponse("Method call processed! currentTime = " + new Date()));
        }
    }

    private void invokeMethod(String json) {
        // TODO: 2023/2/4 现在仅用于register方法
//        ServiceInfo serviceInfo = JsonUtil.fromJson(json, ServiceInfo.class);
//        registryService.register(serviceInfo);
    }

    private FullHttpResponse buildResponse(String s) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(s.getBytes(StandardCharsets.UTF_8)));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONNECTION, "keep-alive");
        return response;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Exception caught!", cause);
        ctx.close();
    }
}
