package com.jerryoops.eurika.provider.server;

public abstract class ProviderServer {

    protected String host;

    protected int port;

    /**
     * 在Spring IOC容器初始化完毕后被调用。
     * 会启动用于接收来自consumer端的RPC调用的服务器。
     */
    public abstract void start();

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }
}
