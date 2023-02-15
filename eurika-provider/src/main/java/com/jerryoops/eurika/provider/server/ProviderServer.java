package com.jerryoops.eurika.provider.server;

public interface ProviderServer {

    /**
     * 在Spring IOC容器初始化完毕后被调用。
     * 会启动用于接收来自consumer端的RPC调用的服务器；
     * 也会调用RegistryService.register方法，将本机Spring IOC中所有被@EurikaService标注的服务注册到注册中心。
     */
    void start();
}
