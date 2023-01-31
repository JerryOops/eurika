package com.jerryoops.eurika.registry.register.impl;

import cn.hutool.core.util.BooleanUtil;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.jerryoops.eurika.common.client.CuratorClient;
import com.jerryoops.eurika.common.constant.ErrorCode;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.domain.exception.BusinessException;
import com.jerryoops.eurika.common.util.ZookeeperUtil;
import com.jerryoops.eurika.registry.register.interfaces.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ZookeeperRegistryService implements RegistryService {
    @Autowired
    private CuratorClient curatorClient;
    @Autowired
    private ZookeeperUtil zookeeperUtil;
    /**
     * 服务注册重试器（单次服务注册失败后，由重试器重新执行注册动作）
     */
    private final Retryer<Boolean> registrationRetryer;
    /**
     * 已注册路径集合：存放本机所有已注册到zookeeper的路径
     */
    private Set<String> registeredPathSet;

    {
        // 构建注册动作重试器
        registrationRetryer = RetryerBuilder.<Boolean>newBuilder()
            .retryIfException(e -> {
                // 情况1：尝试在zookeeper中创建一个已存在的路径，这种情况则跳过所有重试、快速返回失败
                if (e instanceof BusinessException &&
                        ErrorCode.EXCEPTION_PATH_ALREADY_EXISTS.equals( ((BusinessException) e).getCode())) {
                    log.warn(((BusinessException) e).getMsg());
                    return false;
                } else {
                    // 其余所有情况，均继续重试
                    return true;
                }
            })
            .retryIfResult(result -> !BooleanUtil.isTrue(result))
            .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(6))
            .build();
        // 构建已注册路径集合
        registeredPathSet = ConcurrentHashMap.newKeySet();
    }


    /**
     * 将serviceInfo注册到zookeeper。使用registrationRetryer在单次注册失败时进行重试。
     * @param serviceInfo
     * @return
     */
    @Override
    public void register(ServiceInfo serviceInfo) {
        try {
            // 由registrationRetryer执行创建节点的动作，并在创建成功时添加path到registeredPathSet
            registrationRetryer.call(() -> {
                String path = zookeeperUtil.buildProviderPath(serviceInfo);
                boolean nodeCreated = curatorClient.createPersistent(path);
                if (nodeCreated) {
                    registeredPathSet.add(path);
                }
                return nodeCreated;
            });
        } catch (Exception e) {
            log.warn("Exception caught during retry execution", e);
        }
    }

    /**
     * 将registeredPathSet中的路径全部从zookeeper中删除。
     */
    @Override
    public void deregisterAll() {
        if (registeredPathSet.isEmpty()) {
            return;
        }

    }


}
