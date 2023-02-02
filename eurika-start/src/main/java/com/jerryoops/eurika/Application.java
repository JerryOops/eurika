package com.jerryoops.eurika;

import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.util.ApplicationContextUtil;
import com.jerryoops.eurika.configuration.EurikaAppConfiguration;
import com.jerryoops.eurika.provider.server.impl.NettyProviderServer;
import com.jerryoops.eurika.registry.register.interfaces.RegistryService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;


@Import(EurikaAppConfiguration.class)
@ComponentScan
public class Application {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Application.class);
        NettyProviderServer nettyProviderServer = ctx.getBean(NettyProviderServer.class);
        nettyProviderServer.start();
    }
}
