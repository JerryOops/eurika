package com.jerryoops.eurika.common.enumeration;

import lombok.Getter;

/**
 * 传输协议枚举
 */
@Getter
public enum TransmissionProtocolEnum {
    RPC("rpc"),
    HTTP("http")
    ;

    private final String name;

    TransmissionProtocolEnum(String name) {
        this.name = name;
    }
}
