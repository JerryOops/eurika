package com.jerryoops.eurika.registry;

import com.jerryoops.eurika.Application;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.registry.register.interfaces.RegistryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class RegistryServiceTest {

    @Autowired
    RegistryService registryService;

    @Test
    public void testRegistryService() {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setHost("127.0.0.1");
        serviceInfo.setPort(1234);
        serviceInfo.setServiceName("HelloService");
        registryService.register(serviceInfo);
    }
}
