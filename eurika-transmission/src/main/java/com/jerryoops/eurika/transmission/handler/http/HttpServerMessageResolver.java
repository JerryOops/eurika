package com.jerryoops.eurika.transmission.handler.http;

import io.netty.channel.CombinedChannelDuplexHandler;

/**
 * HttpRequestDistiller和HttpResponseBuilder的二合一handler。
 */
public class HttpServerMessageResolver extends CombinedChannelDuplexHandler<HttpRequestDistiller, HttpResponseBuilder> {

    public HttpServerMessageResolver() {
        super();
        super.init(new HttpRequestDistiller(), new HttpResponseBuilder());
    }
}
