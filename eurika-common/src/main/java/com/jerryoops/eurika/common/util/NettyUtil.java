package com.jerryoops.eurika.common.util;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.HOST;

public class NettyUtil {


    public static FullHttpRequest buildHttpRequest(byte[] body, HttpMethod httpMethod) {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, httpMethod, "",
                Unpooled.wrappedBuffer(body));
        request.headers().set(CONTENT_TYPE, "text/plain");
        request.headers().set(CONTENT_LENGTH, request.content().readableBytes());
        request.headers().set(CONNECTION, "keep-alive");
        request.headers().add(ACCEPT, "*/*");
        return request;
    }


    /**
     * 用于构建HttpResponse对象，以body为内容、以status为状态。
     * @param body
     * @param status
     * @return
     */
    public static FullHttpResponse buildHttpResponse(byte[] body, HttpResponseStatus status) {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.wrappedBuffer(body));
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONNECTION, "keep-alive");
        return response;
    }
}
