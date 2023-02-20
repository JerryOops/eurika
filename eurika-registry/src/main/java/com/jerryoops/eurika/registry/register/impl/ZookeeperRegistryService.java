package com.jerryoops.eurika.registry.register.impl;

import cn.hutool.core.util.BooleanUtil;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.util.ZookeeperUtil;
import com.jerryoops.eurika.registry.client.curator.CuratorClient;
import com.jerryoops.eurika.registry.register.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ZookeeperRegistryService implements RegistryService {
    @Autowired
    private CuratorClient curatorClient;
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
                if (e instanceof EurikaException &&
                        ResultCode.EXCEPTION_PATH_ALREADY_EXISTS.getCode()
                                .equals( ((EurikaException) e).getCode())) {
                    log.warn(((EurikaException) e).getMsg());
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
    private void register(ServiceInfo serviceInfo) {
        try {
            // 由registrationRetryer执行创建节点的动作，并在创建成功时添加path到registeredPathSet
            registrationRetryer.call(() -> {
                String path = ZookeeperUtil.buildProviderPath(serviceInfo);
                boolean nodeCreated = curatorClient.createEphemeral(path);
                if (nodeCreated) {
                    registeredPathSet.add(path);
                }
                return nodeCreated;
            });
        } catch (EurikaException ignore) {
            // If curatorClient throws an EurikaException that indicates creation of an existed path, it's ok to ignore it
        } catch (ExecutionException | RetryException e) {
            // Exception thrown by retryer
            log.warn("Retryer exception: {}", e.getMessage());
        }
    }

    /**
     * 将多个serviceInfo注册到zookeeper。
     * @param serviceInfoList
     */
    @Override
    public void register(List<ServiceInfo> serviceInfoList) {
        for (ServiceInfo serviceInfo : serviceInfoList) {
            this.register(serviceInfo);
        }
    }

    /**
     * 将registeredPathSet中的路径全部从zookeeper中删除。
     */
    @Override
    public void deregisterAll() {
        if (registeredPathSet.isEmpty()) {
            log.info("No registered paths exist, nothing needs to be done");
            return;
        }
        curatorClient.delete(registeredPathSet);
        this.registeredPathSet.clear();
    }

    /**
     * 将registeredPathSet中的所有路径重新注册到zookeeper中。
     */
    @Override
    public void reregisterAll() {
        log.info("Start to reregister all services...");
        if (registeredPathSet.isEmpty()) {
            log.info("No registered paths exist, nothing needs to be done");
        }
        // 重新注册的过程中，可能有路径注册失败(createEphemeral返回false)，则需要剔除这部分
        Set<String> newRegisteredPathSet = ConcurrentHashMap.newKeySet(registeredPathSet.size());
        try {
            for (String path : registeredPathSet) {
                registrationRetryer.call(() -> {
                    boolean nodeCreated = curatorClient.createEphemeral(path);
                    if (nodeCreated) {
                        newRegisteredPathSet.add(path);
                    }
                    return nodeCreated;
                });
            }
        } catch (EurikaException ignore) {
            // If curatorClient throws an EurikaException that indicates creation of an existed path, it's ok to ignore it
        } catch (ExecutionException | RetryException e) {
            // Exception thrown by retryer
            log.warn("Retryer exception: {}", e.getMessage());
        } finally {
            this.registeredPathSet = newRegisteredPathSet;
            log.info("Reregistered path: {}", newRegisteredPathSet);
        }
    }

}
