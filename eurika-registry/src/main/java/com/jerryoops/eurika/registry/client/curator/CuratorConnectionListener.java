package com.jerryoops.eurika.registry.client.curator;

import com.jerryoops.eurika.registry.register.interfaces.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CuratorConnectionListener implements ConnectionStateListener {

    @Autowired
    private RegistryService registryService;

    /**
     * 最新的、与zookeeper连接的sessionId
     */
    private long latestSessionId = INITIAL_SESSION_ID;
    private static final long INITIAL_SESSION_ID = -1;

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        long sessionId = INITIAL_SESSION_ID;
        try {
            sessionId = client.getZookeeperClient().getZooKeeper().getSessionId();
        } catch (Exception e) {
            log.warn("Exception occurred during fetching sessionId", e);
        }

        if (newState == ConnectionState.CONNECTED) {
            latestSessionId = sessionId;
            log.warn("state = CONNECTED, sessionId = {}", Long.toHexString(sessionId));

        } else if (newState == ConnectionState.SUSPENDED) {
            log.warn("state = SUSPENDED");

        } else if (newState == ConnectionState.RECONNECTED) {
            // 重新建立到zookeeper的连接：需要判断是否复用同一个session
            if (latestSessionId == sessionId && sessionId != INITIAL_SESSION_ID) {
                // 复用了同一个session，且新获取的sessionId不为INITIAL_SESSION_ID，即说明复用了同一session
                log.warn("state = RECONNECTED, reused sessionId = {}", Long.toHexString(sessionId));
            } else {
                // 重新建立了一个新的session，需要更新latestSessionId
                latestSessionId = sessionId;
                log.warn("state = RECONNECTED, a new session is rebuilt after old session expired," +
                        " old sessionId = {}, new sessionId = {}", Long.toHexString(latestSessionId), Long.toHexString(sessionId));
                registryService.reregisterAll();
            }

        } else if (newState == ConnectionState.LOST) {
            // connection lost, 尝试重建连接
            log.warn("[CuratorConnectionStateListener.stateChanged] state = LOST");
        }
    }
}