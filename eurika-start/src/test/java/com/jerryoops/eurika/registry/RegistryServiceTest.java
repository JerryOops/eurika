package com.jerryoops.eurika.registry;

import com.jerryoops.eurika.Application;
import com.jerryoops.eurika.registry.client.curator.CuratorClient;
import com.jerryoops.eurika.registry.register.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class RegistryServiceTest {

    @Autowired
    RegistryService registryService;

    @Autowired
    CuratorClient curatorClient;

    /**
     * 调试使用：删除/eurika及其下所有子节点
     */
    @Test
    public void deleteAllSubNodesOfEurikaRoot() {
        curatorClient.deleteEverySubNodes();
    }
}
