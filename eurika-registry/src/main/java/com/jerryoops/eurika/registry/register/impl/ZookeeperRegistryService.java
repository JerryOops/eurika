package com.jerryoops.eurika.registry.register.impl;

import cn.hutool.core.util.BooleanUtil;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.jerryoops.eurika.common.client.CuratorClient;
import com.jerryoops.eurika.common.constant.RegistryConstant;
import com.jerryoops.eurika.common.domain.ProviderServiceInfo;
import com.jerryoops.eurika.common.util.ZookeeperUtil;
import com.jerryoops.eurika.registry.register.interfaces.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ZookeeperRegistryService implements RegistryService {

    @Autowired
    CuratorClient curatorClient;
    @Autowired
    RegistryConstant registryConstant;
    @Autowired
    ZookeeperUtil zookeeperUtil;

    Retryer<Boolean> registrationRetryer;

    {
        // 构建注册动作重试器
        registrationRetryer = RetryerBuilder.<Boolean>newBuilder()
            .retryIfException()
            .retryIfResult(result -> !BooleanUtil.isTrue(result))
            .withWaitStrategy(WaitStrategies.fixedWait(1, TimeUnit.SECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(6))
            .build();
    }



    @Override
    public boolean register(ProviderServiceInfo providerServiceInfo) {
        try {
            Boolean result = registrationRetryer.call(() -> {
                String path = zookeeperUtil.buildProviderPath(providerServiceInfo);
                return curatorClient.createPersistent(path);
            });
            if (result) {
                return true;
            }
        } catch (ExecutionException | RetryException e) {
            log.warn("Exception caught during retry execution", e);
            return false;
        }
        return false;
    }

}
