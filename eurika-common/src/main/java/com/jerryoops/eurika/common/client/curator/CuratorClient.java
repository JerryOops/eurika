package com.jerryoops.eurika.common.client.curator;

import com.jerryoops.eurika.common.config.EurikaConfig;
import com.jerryoops.eurika.common.constant.ZookeeperConstant;
import com.jerryoops.eurika.common.domain.exception.BusinessException;
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
    private CuratorConnectionStateListener curatorConnectionStateListener;
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
                throw BusinessException.fail(EXCEPTION_INVALID_PARAM, "Host of registry is blank");
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
                throw BusinessException.fail(EXCEPTION_ZOOKEEPER_CONNECTION_FAILED,
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
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            return true;
        } catch (KeeperException.NodeExistsException e) {
            throw BusinessException.fail(EXCEPTION_PATH_ALREADY_EXISTS, "Node already existed for path: " + path);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
