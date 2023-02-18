package com.jerryoops.eurika.registry;

import com.jerryoops.eurika.Application;
import com.jerryoops.eurika.common.domain.ServiceAnnotationInfo;
import com.jerryoops.eurika.registry.client.curator.CuratorClient;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.registry.register.RegistryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class RegistryServiceTest {

    @Autowired
    RegistryService registryService;

    @Autowired
    CuratorClient curatorClient;

    @Test
    public void testRegistryService() {
    }

    /**
     * 调试使用：删除/eurika及其下所有子节点
     */
    @Test
    public void deleteAllSubNodesOfEurikaRoot() {
        curatorClient.deleteEverySubNodes();
    }
}
