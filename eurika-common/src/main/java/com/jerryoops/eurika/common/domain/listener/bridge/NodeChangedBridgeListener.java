package com.jerryoops.eurika.common.domain.listener.bridge;

import com.jerryoops.eurika.common.domain.ServiceInfo;

/**
 * 用于桥接eurika-consumer和eurika-registry模块的“监听节点动作”，
 * 避免在上层模块(consumer)直接引入注册中心(registry)某种具体实现的监听器，如zookeeper curator的PathChildrenCacheListener。
 */
public interface NodeChangedBridgeListener {

    /**
     * 与注册中心的连接重新建立。
     * <p>对于使用Curator+Zookeeper的注册中心而言，对应于PathChildrenCacheEvent.Type.CONNECTION_RECONNECTED。</p>
     */
    void onConnectionReconnected();

    void onChildAdded(ServiceInfo serviceInfo);

    void onChildRemoved(ServiceInfo serviceInfo);
}
