package com.jerryoops.eurika.provider.server;

public abstract class ProviderServer {

    protected String host;

    protected int port;

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }
}
