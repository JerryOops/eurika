package com.jerryoops.eurika.consumer;

import com.google.common.collect.Sets;
import com.jerryoops.eurika.Application;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.domain.listener.bridge.NodeChangedBridgeListener;
import com.jerryoops.eurika.registry.client.curator.CuratorClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class ConnectionManagerTest {

    @Autowired
    CuratorClient curatorClient;

    @Test
    public void testWatchChildren() {
//        curatorClient.createPersistent("/eurika/DEFAULT_GROUP/HelloService/providers");
        curatorClient.watchChildren("/eurika/DEFAULT_GROUP/HelloService/providers", new NodeChangedBridgeListener() {
            @Override
            public void onConnectionReconnected() {

            }

            @Override
            public void onChildAdded(ServiceInfo serviceInfo) {
                log.info("onChildAdded serviceInfo = {}", serviceInfo);
            }

//            @Override
//            public void onChildUpdated(ServiceInfo serviceInfo) {
//                log.info("onChildUpdated serviceInfo = {}", serviceInfo);
//            }

            @Override
            public void onChildRemoved(ServiceInfo serviceInfo) {
                log.info("onChildRemoved serviceInfo = {}", serviceInfo);
            }
        });

        // added
        curatorClient.createEphemeral("/eurika/DEFAULT_GROUP/HelloService/providers/{\"host\":\"127.0.0.1\", \"port\":90, \"version\":\"1\"}");
        // updated
//        curatorClient.update("/eurika/DEFAULT_GROUP/HelloService/providers/{\"host\":\"127.0.0.1\", \"port\":90, \"version\":\"1\"}", "val");
        // removed
        curatorClient.delete(Sets.newHashSet(Collections.singletonList("/eurika/DEFAULT_GROUP/HelloService/providers/{\"host\":\"127.0.0.1\", \"port\":90, \"version\":\"1\"}")));
    }


    @Test
    public void testGetChildren() {
        List<String> children = curatorClient.getChildren("/eurika/DEFAULT_GROUP/HelloService/consumers");
        System.out.println(children);
    }
}
