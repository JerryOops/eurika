package com.jerryoops.eurika.registry.client.curator;

import com.jerryoops.eurika.common.constant.ZookeeperConstant;
import com.jerryoops.eurika.common.domain.ProviderLeafNode;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.domain.config.RegistryConfig;
import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.domain.listener.bridge.NodeChangedBridgeListener;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.tool.config.ConfigManager;
import com.jerryoops.eurika.common.util.ZookeeperUtil;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CONNECTION_RECONNECTED;
import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CHILD_ADDED;
import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CHILD_UPDATED;
import static org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type.CHILD_REMOVED;


@NoArgsConstructor
@Slf4j
@Component
public class CuratorClient {
    @Autowired
    private CuratorConnectionListener curatorConnectionStateListener;
    private CuratorFramework client;

    // 已加入监听的path之set集合。对应所有被@EurikaReference修饰的service在zookeeper中的role-depth路径。
    private final Set<String> watchedPathSet = ConcurrentHashMap.newKeySet();

    /**
     * Curator连接的初始化
     */
    @PostConstruct
    private void init() {
        RegistryConfig registryConfig = ConfigManager.getRegistryConfig();
        // 变量
        Integer connectionTimeoutMillis = registryConfig.getTimeoutConnection();
        if (null == connectionTimeoutMillis || connectionTimeoutMillis < 0) {
            connectionTimeoutMillis = ZookeeperConstant.DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;
        }
        Integer sessionTimeoutMillis = registryConfig.getTimeoutSession();
        if (null == sessionTimeoutMillis || sessionTimeoutMillis < 0) {
            sessionTimeoutMillis = ZookeeperConstant.DEFAULT_SESSION_TIMEOUT_MILLISECONDS;
        }
        try {
            // 建立连接到registryAddress的curatorClient
            String registryAddress = registryConfig.getAddress();
            if (StringUtils.isBlank(registryAddress)) {
                throw EurikaException.fail(ResultCode.EXCEPTION_INVALID_PARAM, "Host of registry is blank");
            }
            client = CuratorFrameworkFactory.builder()
                    .connectString(registryAddress)
                    .connectionTimeoutMs(connectionTimeoutMillis)
                    .sessionTimeoutMs(sessionTimeoutMillis)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .build();
            client.getConnectionStateListenable().addListener(curatorConnectionStateListener);
            client.start();
        } catch (Exception e) {
            client.close();
            log.error("Curator initialization failed!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 在zookeeper中为指定路径创建永久节点
     * @param path zookeeper路径
     * @return
     */
    public boolean createPersistent(String path) {
        return this.createNode(path, CreateMode.PERSISTENT);
    }

    /**
     * 在zookeeper中为指定路径创建临时节点
     * @param path zookeeper路径
     * @return
     */
    public boolean createEphemeral(String path) {
        return this.createNode(path, CreateMode.EPHEMERAL);
    }

    private boolean createNode(String path, CreateMode createMode) {
        try {
            client.create().creatingParentsIfNeeded().withMode(createMode).forPath(path);
            return true;
        } catch (KeeperException.NodeExistsException e) {
            throw EurikaException.fail(ResultCode.EXCEPTION_PATH_ALREADY_EXISTS, "Node already existed for path: " + path);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 在zookeeper中删除pathSet指定的路径叶节点。如果path上的非叶节点没有其他有效子节点时，该非叶节点也会被一同删除。
     * @param pathSet
     */
    public void delete(Set<String> pathSet) {
        for (String path : pathSet) {
            try {
                client.delete().forPath(path);
            } catch (Exception e) {
                log.info("Exception occurred when deleting path: {}", path, e);
            }
        }
        log.info("All paths deleted!");
    }


    /**
     * 获取path之下的所有子节点。
     * <p>如果path合法，但其下无子节点：则会返回空list；</p>
     * <p>如果path不合法（zookeeper中不存在此路径），或者在执行过程中发生其它错误，
     * 则将会爆出exception。此处约定返回null（区别于空list）。</p>
     * @param path
     * @return
     */
    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch(Exception e) {
            log.error("Exception caught during getting children for path: {}", path, e);
            return null;
        }
    }


    /**
     * 为path注册一个监听器listener，在path之下的子节点发生变更事件时，使用listener进行通知。
     * @param path role-depth path，即以/providers结尾的zookeeper路径
     * @param bridgeListener
     */
    public void watchChildren(String path, NodeChangedBridgeListener bridgeListener) {
        if (watchedPathSet.contains(path)) {
            // 如针对一个已经加入监听状态的path重复调用了watchChildren，则忽略之
            return;
        }
        watchedPathSet.add(path);
        PathChildrenCache cache = new PathChildrenCache(client, path, true);
        try {
            cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            log.error("Exception caught during starting PathChildrenCache instance to watch path [{}]", path, e);
            return;
        }
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                log.info("child event occurred, type = {}, childData = {}", event.getType(), event.getData());
                if (CONNECTION_RECONNECTED.equals(event.getType())) {
                    // TODO: 2023/3/3 update
                    return;
                }
                ChildData childData = event.getData();
                ServiceInfo serviceInfo = ZookeeperUtil.parseFullDepthPath(childData.getPath());
                if (CHILD_ADDED.equals(event.getType())) {
                    bridgeListener.onChildAdded(serviceInfo);
                } else if (CHILD_REMOVED.equals(event.getType())) {
                    bridgeListener.onChildRemoved(serviceInfo);
                }
            }
        });
    }




    /**
     * For unit-test only, will be deleted later
     */
    public void deleteEverySubNodes() {
        try {
            client.delete().deletingChildrenIfNeeded().forPath(ZookeeperConstant.EURIKA_ROOT_PATH);
        } catch (Exception e) {
            log.warn("在删除所有节点过程中捕获异常：", e);
        }
    }



}
