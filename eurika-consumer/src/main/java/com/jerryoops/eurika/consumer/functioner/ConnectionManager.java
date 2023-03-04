package com.jerryoops.eurika.consumer.functioner;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.jerryoops.eurika.common.domain.ConnectionInfo;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.domain.listener.bridge.NodeChangedBridgeListener;
import com.jerryoops.eurika.common.util.StringEscapeUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.jerryoops.eurika.common.constant.ProviderConstant.SERVICE_BEAN_MAP_KEY_SEPARATOR;

/**
 * 负责定期调用serviceDiscoverer从注册中心拉取最新的服务信息、并维护一份存储在本地的服务信息表(缓存)。
 */
@Component
@Slf4j
public class ConnectionManager {

    @Autowired
    private ServiceDiscoverer serviceDiscoverer;

    /**
     * 被@EurikaReference标注，且在注册中心找到路径的service信息连接信息map。
     * 注意value list可能为non-null empty。
     * <p>name#group#version(service specification) --> Lists.of(ConnectionInfo...) </p>
     */
    private final Map<String, List<ConnectionInfo>> connectionInfoMap;

    /**
     * 被@EurikaReference标注，但是在注册中心不存在的路径对应的service的信息列表
     */
    private final List<ServiceInfo> unconnectedServiceList;

    // 用于监听关心的服务provider节点的变化（添加、删除），并同步至connectionInfoMap中。
    private final NodeChangedBridgeListener listener;

    {
        connectionInfoMap = new ConcurrentHashMap<>();
        unconnectedServiceList = new ArrayList<>();
        listener = new NodeChangedBridgeListener() {
            @Override
            public void onConnectionReconnected() {
                // TODO: 2023/3/3  to be filled
            }
            @Override
            public void onChildAdded(ServiceInfo serviceInfo) {
                String key = generateKey(serviceInfo.getServiceName(), serviceInfo.getGroup(), serviceInfo.getVersion());
                // 此处connectionInfo.channel为null，只获取host/port，channel进行懒加载
                ConnectionInfo connectionInfo = BeanUtil.copyProperties(serviceInfo, ConnectionInfo.class);
                appendPut(key, connectionInfo);
            }

            @Override
            public void onChildRemoved(ServiceInfo serviceInfo) {
                String key = generateKey(serviceInfo.getServiceName(), serviceInfo.getGroup(), serviceInfo.getVersion());
                remove(key);
            }
        };
    }


    //----------------------对外方法-----------------------------
    /**
     * 被EurikaAnnotationPostProcessor调用，以进行初始化：
     * 从注册中心中拉取所有被@EurikaReference修饰的服务之信息，存储到connectionInfoMap中。
     */
    public void addConnection(String className, String group, String version) {
        String key = this.generateKey(className, group, version);
        if (connectionInfoMap.containsKey(key)) {
            // className, group, version三者合一时，唯一对应的service在注册中心中的存在已经被扫描过了
            return;
        }
        List<ConnectionInfo> connectionInfoList = serviceDiscoverer.doDiscover(className, group, version);
        if (null == connectionInfoList) {
            // 给定的className,group生成的路径在注册中心内不存在。即没有provider暴露此service，或者能执行此service的provider还未上线。
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceName(className);
            serviceInfo.setGroup(group);
            serviceInfo.setVersion(version);
            unconnectedServiceList.add(serviceInfo);
        } else {
            this.connectionInfoMap.put(key, connectionInfoList);
            serviceDiscoverer.doWatchProviders(className, group, this.listener);
        }
    }



    /**
     * 获取所有对外提供指定服务的provider对应的连接(channel)，使用负载均衡从中选取一个返回。
     * 如果该channel暂不存在(尚未连接该provider)，则进行连接。
     * @return
     */
    public Channel getChannel(String serviceName, String group, String version) {
        // 检查本地缓存中是否有符合要求的provider之channel
        String key = this.generateKey(serviceName, group, version);
        List<ConnectionInfo> connectionInfos = connectionInfoMap.get(key);
        if (CollectionUtil.isEmpty(connectionInfos)) {
            // TODO: 2023/3/3 尝试调用serviceDiscoverer进行拉取（connectionInfoMap需要由serviceDiscover监听变化）
        }
        // TODO: 2023/3/3 return loadbalancer.choose(connectionInfos)
        return null;
    }


    //----------------------内部方法-----------------------------

    private void appendPut(String key, ConnectionInfo connectionInfo) {
        List<ConnectionInfo> list = connectionInfoMap.getOrDefault(key, new ArrayList<>());
        list.add(connectionInfo);
        connectionInfoMap.put(key, list);
    }

    private void remove(String key) {
        connectionInfoMap.remove(key);
    }

    /**
     * 将入参进行转义(仅对#进行转义)后，添加'#'分隔符，生成serviceInfoMap的key
     * @param className 未转义className
     * @param group 未转义group
     * @param version 未转义version
     * @return
     */
    private String generateKey(String className, String group, String version) {
        return StringEscapeUtil.escapeHashKey(className) + SERVICE_BEAN_MAP_KEY_SEPARATOR +
                StringEscapeUtil.escapeHashKey(group) + SERVICE_BEAN_MAP_KEY_SEPARATOR +
                StringEscapeUtil.escapeHashKey(version);
    }
}
