package com.jerryoops.eurika.consumer.functioner;

import com.jerryoops.eurika.common.domain.ConnectionInfo;
import com.jerryoops.eurika.common.domain.listener.bridge.NodeChangedBridgeListener;
import com.jerryoops.eurika.registry.discover.DiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 负责与eurika-registry模块的DiscoveryService直接交互的实例。
 */
@Component
@Slf4j
public class ServiceDiscoverer {

    @Autowired
    private DiscoveryService discoveryService;

    //----------------------对外方法-----------------------------
    /**
     * 进行服务发现，从注册中心拉取指定参数对应的所有provider信息。
     * <p>返回null: 给定的className,group生成的路径在注册中心内不存在。</p>
     * <p>返回空list: 给定的className,group生成的路径在注册中心中存在，但是没有provider提供此服务。</p>
     * <p>返回非空list: 给定的className,group生成的路径在注册中心中存在，且存在provider提供此服务。</p>
     * @param className
     * @param group
     * @param version
     * @return
     */
    public List<ConnectionInfo> doDiscover(String className, String group, String version) {
        return discoveryService.discover(className, group, version);
    }

    public void doWatchProviders(String className, String group, NodeChangedBridgeListener listener) {
        discoveryService.watchProviders(className, group, listener);
    }
}
