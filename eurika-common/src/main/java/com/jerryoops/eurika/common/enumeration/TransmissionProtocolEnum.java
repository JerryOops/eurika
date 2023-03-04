package com.jerryoops.eurika.common.enumeration;

import lombok.Getter;

/**
 * 传输协议枚举
 */
@Getter
public enum TransmissionProtocolEnum {
    RPC("rpc"),
    /**
     * 使用HTTP 1.1作为传递RPC信息的协议，使用Netty提供的HTTP编解码类/序列化工具。
     * 优点在于无须自行实现编解码的细节；缺点在于HTTP框架较为沉重，效率偏低。
     */
    HTTP("http")
    ;

    private final String name;

    TransmissionProtocolEnum(String name) {
        this.name = name;
    }


    public static TransmissionProtocolEnum getByName(String name) {
        for (TransmissionProtocolEnum t : TransmissionProtocolEnum.values()) {
            if (t.name.equals(name)) {
                return t;
            }
        }
        return null;
    }
}
