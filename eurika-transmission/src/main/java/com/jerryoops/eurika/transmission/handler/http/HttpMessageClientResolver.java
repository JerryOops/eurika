package com.jerryoops.eurika.transmission.handler.http;

import io.netty.channel.CombinedChannelDuplexHandler;

public class HttpMessageClientResolver extends CombinedChannelDuplexHandler<HttpResponseDistiller, HttpRequestInstiller> {

    public HttpMessageClientResolver() {
        super();
        super.init(new HttpResponseDistiller(), new HttpRequestInstiller());
    }
}
