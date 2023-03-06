package com.jerryoops.eurika.consumer.functioner;

import cn.hutool.core.bean.BeanUtil;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jerryoops.eurika.common.domain.ConnectionInfo;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.domain.config.ConsumerConfig;
import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.domain.listener.bridge.NodeChangedBridgeListener;
import com.jerryoops.eurika.common.enumeration.LoadBalanceEnum;
import com.jerryoops.eurika.common.tool.config.ConfigManager;
import com.jerryoops.eurika.common.tool.loadbalance.LoadBalancer;
import com.jerryoops.eurika.common.util.StringEscapeUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
     * <p>name#group#version(service specification) --> Sets.of(ConnectionInfo...) </p>
     */
    private final Map<String, List<ConnectionInfo>> connectedMap;

    /**
     * 被@EurikaReference标注，但是在注册中心不存在的路径对应的service的信息map。
     */
    // TODO: 2023/3/4 daemon线程定时重试
    private final Map<String, ServiceInfo> unconnectedMap;
    /**
     * 用于在连接provider、创建channel时的失败重试。
     */
    private final Retryer<Channel> connectionRetryer;
    /**
     * 用于监听关心的服务provider节点的变化（添加、删除），并同步至connectedMap中。
     */
    private final NodeChangedBridgeListener listener;

    {
        connectedMap = new ConcurrentHashMap<>();
        unconnectedMap = new ConcurrentHashMap<>();
        connectionRetryer = RetryerBuilder.<Channel>newBuilder()
                .retryIfException()
                .retryIfResult(Objects::isNull)
                .withWaitStrategy(WaitStrategies.fixedWait(500, TimeUnit.MILLISECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .build();
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
                ConnectionInfo connectionInfo = BeanUtil.copyProperties(serviceInfo, ConnectionInfo.class);
                remove(key, connectionInfo);
            }
        };
    }


    //----------------------对外方法-----------------------------
    /**
     * 被EurikaAnnotationPostProcessor调用，以进行初始化：
     * 从注册中心中拉取所有被@EurikaReference修饰的服务之信息，存储到connectedMap中。
     * <p>注意：仅允许被EurikaAnnotationPostProcessor在初始化@EurikaReference时调用。</p>
     */
    public void addConnection(String serviceName, String group, String version) {
        String key = this.generateKey(serviceName, group, version);
        if (connectedMap.containsKey(key) || unconnectedMap.containsKey(key)) {
            // serviceName, group, version三者合一时: 唯一对应的service在注册中心中的存在已经被扫描过了，
            // 要么存在于connectedMap中，要么存在于unconnectedMap中，任一种可能都无须重新从注册中心再次拉取信息
            return;
        }
        // 从注册中心中拉取service, group, version对应的连接信息
        List<ConnectionInfo> connectionInfoList = serviceDiscoverer.doDiscover(serviceName, group, version);
        if (null == connectionInfoList) {
            // 给定的className,group生成的路径在注册中心内不存在。即没有provider暴露此service，或者能执行此service的provider还未上线。
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceName(serviceName);
            serviceInfo.setGroup(group);
            serviceInfo.setVersion(version);
            unconnectedMap.put(key, serviceInfo);
        } else {
            connectedMap.put(key, connectionInfoList);
            serviceDiscoverer.doWatchProviders(serviceName, group, this.listener);
        }
    }


    /**
     * 获取所有对外提供指定服务的provider对应的连接(channel)，使用负载均衡从中选取一个返回。
     * 如果该channel暂不存在(尚未连接该provider)，则进行连接。
     * @param bootstrap NettyConsumerClient的bootstrap，用于在channel不存在时创建连接。
     * @param request 准备发出的RpcRequest
     * @return
     */
    public Channel getChannel(Bootstrap bootstrap, RpcRequest request) {
        final String serviceName = request.getClassName();
        final String group = request.getGroup();
        final String version = request.getVersion();

        // 检查本地缓存中是否有符合要求的provider之channel
        String key = this.generateKey(serviceName, group, version);
        if (unconnectedMap.containsKey(key)) {
            // 初始化时，指定的service信息是非法的(在注册中心内不存在)、或者getChildren时出现异常：
            // path被加入了unconnectedMap中，并且没有注册子节点listener。现尝试在注册中心中重找、注册子节点listener一次。
            List<ConnectionInfo> connectionInfoList = serviceDiscoverer.doDiscover(serviceName, group, version);
            if (null == connectionInfoList) {
                // path在注册中心仍然不存在
                log.warn("Given service specification is not found within registry center: " +
                        "[serviceName = {}, group = {}, version = {}]", serviceName, group, version);
                return null;
            }
            // path在注册中心存在了
            unconnectedMap.remove(key);
            connectedMap.put(key, connectionInfoList);
            serviceDiscoverer.doWatchProviders(serviceName, group, this.listener);
        }

        List<ConnectionInfo> connectionInfoList = connectedMap.get(key);
        if (null == connectionInfoList || connectionInfoList.isEmpty()) {
            // null: 传入的service信息是非法的，因为它既不在unconnectedMap中，也不在connectedMap中。
            // isEmpty: 传入的service信息是合法的，并且在connectedMap中，但是没有provider提供该服务。
            log.error("The service specification itself is invalid, " +
                    "or there exists no available providers corresponding to it currently: " +
                    "[serviceName = {}, group = {}, version = {}]", serviceName, group, version);
            return null;
        }
        ConnectionInfo connectionInfo;
        ConsumerConfig config = ConfigManager.getConsumerConfig();
        if (LoadBalanceEnum.CONSISTENT_HASH.getName().equals(config.getLoadbalance())) {
            connectionInfo = LoadBalancer.CONSISTENT_HASH.select(key, request.getMethodName(), connectionInfoList);
        } else {
            connectionInfo = LoadBalancer.RANDOM.select(connectionInfoList);
        }
        Channel channel = connectionInfo.getChannel();
        if (null != channel && channel.isActive()) {
            return channel;
        }
        this.initChannel(bootstrap, connectionInfo);
        return connectionInfo.getChannel();
    }





    //----------------------内部方法-----------------------------

    /**
     * 将给定connectionInfo的channel进行初始化、并设置进入该实例对象中，然后将channel返回。
     * @param connectionInfo
     * @return
     */
    private void initChannel(Bootstrap bootstrap, ConnectionInfo connectionInfo) {
        try {
            connectionRetryer.call(() -> {
                Channel channel = bootstrap.connect(connectionInfo.getHost(), connectionInfo.getPort()).sync().channel();
                connectionInfo.setChannel(channel);
                return channel;
            });
        } catch (ExecutionException | RetryException e) {
            log.warn("Exception occurred when trying to connect to provider", e);
        }
    }


    /**
     * 用于在监听到注册中心内的某条role-depth路径下发生了增加子节点事件时，将新加入的connectionInfo同步到connectedMap中。
     * @param key
     * @param connectionInfo
     */
    private void appendPut(String key, ConnectionInfo connectionInfo) {
        connectedMap.compute(key, (k, v) -> {
            if (null == v) {
                // 不存在与key对应的value
                return Lists.newArrayList(connectionInfo);
            } else {
                v.add(connectionInfo);
                return v;
            }
        });
    }

    /**
     * 用于在监听到注册中心内的某条role-depth路径下发生了删除子节点事件时，将删除动作同步到connectedMap中。
     * @param key
     * @param connectionInfo
     */
    private void remove(String key, ConnectionInfo connectionInfo) {
        synchronized (connectedMap) {
            List<ConnectionInfo> connectionInfoList = connectedMap.get(key);
            if (null == connectionInfoList) {
                log.warn("Attempting to remove an entry from connectedMap using non-existed key: {}", key);
                return;
            }
            if (connectionInfoList.isEmpty()) {
                connectedMap.remove(key);
                return;
            }
            // ConnectionInfo的equals方法被重写为只考虑host/port属性，
            // 因此可以直接调用remove()方法删除connectionInfoList中具有与connectionInfo相同host/port的元素
            connectionInfoList.remove(connectionInfo);
        }
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
