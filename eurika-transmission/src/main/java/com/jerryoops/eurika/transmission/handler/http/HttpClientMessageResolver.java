package com.jerryoops.eurika.transmission.handler.http;

import io.netty.channel.CombinedChannelDuplexHandler;

public class HttpClientMessageResolver extends CombinedChannelDuplexHandler<HttpResponseDistiller, HttpRequestBuilder> {

    public HttpClientMessageResolver() {
        super();
        super.init(new HttpResponseDistiller(), new HttpRequestBuilder());
    }
}
