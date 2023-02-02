package com.jerryoops.eurika.registry.client.curator;

import com.jerryoops.eurika.common.config.EurikaConfig;
import com.jerryoops.eurika.common.constant.ZookeeperConstant;
import com.jerryoops.eurika.common.domain.exception.EurikaException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.jerryoops.eurika.common.constant.ErrorCode.EXCEPTION_INVALID_PARAM;
import static com.jerryoops.eurika.common.constant.ErrorCode.EXCEPTION_PATH_ALREADY_EXISTS;
import static com.jerryoops.eurika.common.constant.ErrorCode.EXCEPTION_ZOOKEEPER_CONNECTION_FAILED;


@NoArgsConstructor
@Slf4j
@Component
public class CuratorClient {
    @Autowired
    private EurikaConfig eurikaConfig;
    @Autowired
    private CuratorConnectionListener curatorConnectionStateListener;
    private CuratorFramework client;

    /**
     * Curator连接的初始化。如发生异常，则在重试达到最大上限后抛出错误。
     */
    @PostConstruct
    private void init() {
        // 变量
        Integer connectionTimeoutMillis = eurikaConfig.getRegistryConnectionTimeoutMilliseconds();
        if (null == connectionTimeoutMillis || connectionTimeoutMillis < 0) {
            connectionTimeoutMillis = ZookeeperConstant.DEFAULT_CONNECTION_TIMEOUT_MILLISECONDS;
        }
        Integer sessionTimeoutMillis = eurikaConfig.getRegistrySessionTimeoutMilliseconds();
        if (null == sessionTimeoutMillis || sessionTimeoutMillis < 0) {
            sessionTimeoutMillis = ZookeeperConstant.DEFAULT_SESSION_TIMEOUT_MILLISECONDS;
        }
        try {
            // 建立连接到registryAddress的curatorClient
            String registryAddress = eurikaConfig.getRegistryAddress();
            if (StringUtils.isBlank(registryAddress)) {
                throw EurikaException.fail(EXCEPTION_INVALID_PARAM, "Host of registry is blank");
            }
            client = CuratorFrameworkFactory.builder()
                    .connectString(registryAddress)
                    .connectionTimeoutMs(connectionTimeoutMillis)
                    .sessionTimeoutMs(sessionTimeoutMillis)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .build();
            client.getConnectionStateListenable().addListener(curatorConnectionStateListener);
            client.start();
            // 阻塞检查连接状态
            boolean isConnected = client.blockUntilConnected(connectionTimeoutMillis, TimeUnit.MILLISECONDS);
            if (!isConnected) {
                // 连接到zookeeper注册中心失败
                throw EurikaException.fail(EXCEPTION_ZOOKEEPER_CONNECTION_FAILED,
                        "Exceeded maximum block-waiting time, status remained unconnected");
            }
            log.info("CuratorClient successfully built!");
        } catch (Exception e) {
            client.close();
            log.error("Curator initialization failed!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 在zookeeper中为指定路径创建永久节点
     * @param path
     * @return
     */
    public boolean createPersistent(String path) {
        return this.createNode(path, CreateMode.PERSISTENT);
    }

    /**
     * 在zookeeper中为指定路径创建临时节点
     * @param path
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
            throw EurikaException.fail(EXCEPTION_PATH_ALREADY_EXISTS, "Node already existed for path: " + path);
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
