package com.jerryoops.eurika.transmission.handler.http;

import io.netty.channel.CombinedChannelDuplexHandler;

public class HttpServerMessageResolver extends CombinedChannelDuplexHandler<HttpRequestDistiller, HttpResponseBuilder> {

    public HttpServerMessageResolver() {
        super();
        super.init(new HttpRequestDistiller(), new HttpResponseBuilder());
    }
}
