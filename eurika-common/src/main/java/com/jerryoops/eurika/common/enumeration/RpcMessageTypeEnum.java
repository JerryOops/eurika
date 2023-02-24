package com.jerryoops.eurika.common.enumeration;

import lombok.Getter;

@Getter
public enum RpcMessageTypeEnum {
    /**
     * RpcMessage类型：RpcRequest
     */
    RPC_REQUEST("RpcRequest", 1),
    /**
     * RpcMessage类型：RpcResponse
     */
    RPC_RESPONSE("RpcResponse", 2)
    ;

    /**
     * 名称
     */
    private final String name;
    /**
     * 内部编码
     */
    private final Integer code;

    RpcMessageTypeEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public static boolean isValid(Byte code) {
        return get(code) != null;
    }

    public static RpcMessageTypeEnum get(Byte code) {
        for (RpcMessageTypeEnum r : RpcMessageTypeEnum.values()) {
            if (r.code.equals((int) code)) {
                return r;
            }
        }
        return null;
    }
}
