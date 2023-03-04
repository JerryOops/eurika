package com.jerryoops.eurika.registry.discover.impl;

import com.jerryoops.eurika.common.domain.ConnectionInfo;
import com.jerryoops.eurika.common.domain.ProviderLeafNode;
import com.jerryoops.eurika.common.domain.listener.bridge.NodeChangedBridgeListener;
import com.jerryoops.eurika.common.util.ZookeeperUtil;
import com.jerryoops.eurika.registry.client.curator.CuratorClient;
import com.jerryoops.eurika.registry.discover.DiscoveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ZookeeperDiscoveryService implements DiscoveryService {

    @Autowired
    CuratorClient curatorClient;

    /**
     * 获取className,group,version指定的所有服务的信息，转为ConnectionInfoList返回。
     * @param className
     * @param group
     * @param version
     * @return
     */
    @Override
    public List<ConnectionInfo> discover(String className, String group, String version) {
        String path = ZookeeperUtil.buildRoleDepth(group, className); // role-depth path
        // 获取所有叶节点，转为connectionInfoList
        List<String> leafNodes = curatorClient.getChildren(path);
        if (null == leafNodes) {
            // path不合法，或者在getChildren中捕获了其它exception
            return null;
        }
        return leafNodes.stream()
                .filter(Objects::nonNull)
                .map(ProviderLeafNode::parse)
                .filter(x -> Objects.equals(version, x.getVersion()))
                .map(this::convertToConnectionInfo)
                .collect(Collectors.toList());
    }


    /**
     * 监听className和group之下的所有providers的节点变动情况，并在发生变化时通知给定的listener实例。
     * <p>如果className和group生成的路径path不合法（在zookeeper中不存在），或者path已经注册了监听，则不会做任何事情。</p>
     * @param className
     * @param group
     * @param listener
     */
    @Override
    public void watchProviders(String className, String group, NodeChangedBridgeListener listener) {
        String path = ZookeeperUtil.buildRoleDepth(group, className); // role-depth path
        curatorClient.watchChildren(path, listener);
    }



    /**
     * 用于将ProviderLeafNode形式的POJO转化为ConnectionInfo的POJO。
     * @param leafNode
     * @return
     */
    private ConnectionInfo convertToConnectionInfo(ProviderLeafNode leafNode) {
        ConnectionInfo connectionInfo = new ConnectionInfo();
        connectionInfo.setHost(leafNode.getHost());
        connectionInfo.setPort(leafNode.getPort());
        return connectionInfo;
    }
}
