package com.jerryoops.eurika;

import com.jerryoops.eurika.configuration.ApplicationConfiguration;
import com.jerryoops.eurika.consumer.client.impl.NettyConsumerClient;
import com.jerryoops.eurika.provider.server.impl.NettyProviderServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ComponentScan
public class Application {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        threadPool.submit(() -> {
            NettyProviderServer nettyProviderServer = ctx.getBean(NettyProviderServer.class);
            nettyProviderServer.start();
        });
        threadPool.submit(() -> {
            NettyConsumerClient nettyConsumerClient = ctx.getBean(NettyConsumerClient.class);
            nettyConsumerClient.start();
        });
    }
}
