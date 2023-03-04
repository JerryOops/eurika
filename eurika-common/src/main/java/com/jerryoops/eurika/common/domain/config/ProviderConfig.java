package com.jerryoops.eurika.common.domain.config;

import com.jerryoops.eurika.common.constant.PropertyConstant;
import com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum;
import lombok.Getter;

@Getter
public class ProviderConfig extends Config {
    {
        super.prefix = PropertyConstant.PROVIDER_CONFIG_PREFIX;
    }

    /**
     * provider的传输协议。
     * <p>key = {super.prefix} + .protocol </p>
     * <p>value = a string that represents a kind of transmission protocol, e.g. http, rpc </p>
     * <p>defaultValue = http </p>
     */
    private String protocol = TransmissionProtocolEnum.HTTP.getName();
}
