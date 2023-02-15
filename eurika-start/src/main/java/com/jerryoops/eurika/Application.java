package com.jerryoops.eurika;

import com.jerryoops.eurika.configuration.ApplicationConfiguration;
import com.jerryoops.eurika.provider.server.impl.NettyProviderServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Import(ApplicationConfiguration.class)
@ComponentScan
public class Application {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        threadPool.submit(() -> {
            NettyProviderServer nettyProviderServer = ctx.getBean(NettyProviderServer.class);
            nettyProviderServer.start();
        });
//        threadPool.submit(() -> {
//            // TODO: 2023/2/4 设置一个config，如果为true则启动本地调试接口
//            // 启动test server
//            TestServer testServer = ctx.getBean(TestServer.class);
//            testServer.start();
//        });
    }
}
