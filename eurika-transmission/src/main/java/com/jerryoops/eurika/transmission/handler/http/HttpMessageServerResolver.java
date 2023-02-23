package com.jerryoops.eurika.transmission.handler.http;

import io.netty.channel.CombinedChannelDuplexHandler;

public class HttpMessageServerResolver extends CombinedChannelDuplexHandler<HttpRequestDistiller, HttpResponseBuilder> {

    public HttpMessageServerResolver() {
        super();
        super.init(new HttpRequestDistiller(), new HttpResponseBuilder());
    }
}
