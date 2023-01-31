package com.jerryoops.eurika.common.client;

import com.jerryoops.eurika.common.config.EurikaConfig;
import com.jerryoops.eurika.common.constant.RegistryConstant;
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


@NoArgsConstructor
@Slf4j
@Component
public class CuratorClient {
    @Autowired
    private EurikaConfig eurikaConfig;

    @Autowired
    private ZookeeperConstant zookeeperConstant;

    private CuratorFramework client;


    /**
     * Curator连接的初始化。如发生异常，则在重试达到最大上限后抛出错误。
     */
    @PostConstruct
    private void init() throws InterruptedException {
        try {
            // 变量
            Integer maxWaitMillis = eurikaConfig.getRegistryMaxWaitMilliseconds();
            if (null == maxWaitMillis || maxWaitMillis < 0) {
                maxWaitMillis = RegistryConstant.DEFAULT_MAX_WAIT_MILLISECONDS;
                log.info("Unspecified or invalid value for registry wait milliseconds, will use default value instead: " + maxWaitMillis);
            }
            // 建立连接到registryAddress的curatorClient
            String registryAddress = eurikaConfig.getRegistryAddress();
            if (StringUtils.isBlank(registryAddress)) {
                throw BusinessException.fail(EXCEPTION_INVALID_PARAM, "Host of registry is blank");
            }
            client = CuratorFrameworkFactory.builder()
                    .connectString(registryAddress)
                    .sessionTimeoutMs(5000) // TODO: 2023/1/31 作为config传入
                    .connectionTimeoutMs(maxWaitMillis)
                    .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                    .build();
            client.start();
            // 阻塞检查连接状态
            boolean isConnected = client.blockUntilConnected(maxWaitMillis, TimeUnit.MILLISECONDS);
            if (!isConnected) {
                // 连接到zookeeper注册中心失败
                throw new IllegalStateException("Exceeded maximum block-waiting time, status remained unconnected");
            }
            log.info("CuratorClient successfully built!");
        } catch (Exception e) {
            this.close();
            log.error("Curator initialization failed!", e);
            throw e;
        }
    }

    public boolean createPersistent(String path) {
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            return true;
        } catch (KeeperException.NodeExistsException e) {
            log.warn("Node already existed for path: {}", path);
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * shut down everything
     */
    private void close() {
        // TODO: 2023/1/31 清除所有已注册到zk的path
        client.close();
    }
}
