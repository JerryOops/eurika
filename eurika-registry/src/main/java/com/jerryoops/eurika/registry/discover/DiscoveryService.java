package com.jerryoops.eurika.registry.discover;


import com.jerryoops.eurika.common.domain.ConnectionInfo;
import com.jerryoops.eurika.common.domain.listener.bridge.NodeChangedBridgeListener;

import java.util.List;

public interface DiscoveryService {

    List<ConnectionInfo> discover(String className, String group, String version);

    void watchProviders(String className, String group, NodeChangedBridgeListener listener);
}
