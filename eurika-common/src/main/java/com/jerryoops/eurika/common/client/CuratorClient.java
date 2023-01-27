package com.jerryoops.eurika.common.client;

import com.jerryoops.eurika.common.config.EurikaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CuratorClient {
    @Autowired
    EurikaConfig eurikaConfig;

}
