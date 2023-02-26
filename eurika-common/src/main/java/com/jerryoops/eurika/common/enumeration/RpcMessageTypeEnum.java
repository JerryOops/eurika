package com.jerryoops.eurika.common.enumeration;

import lombok.Getter;

@Getter
public enum RpcMessageTypeEnum {
    /**
     * RpcMessage类型：RpcRequest
     */
    RPC_REQUEST("RpcRequest", (byte) 1),
    /**
     * RpcMessage类型：RpcResponse
     */
    RPC_RESPONSE("RpcResponse", (byte) 2)
    ;

    /**
     * 名称
     */
    private final String name;
    /**
     * 内部编码
     */
    private final Byte code;

    RpcMessageTypeEnum(String name, Byte code) {
        this.name = name;
        this.code = code;
    }

    public static boolean isValid(Byte code) {
        return get(code) != null;
    }

    public static RpcMessageTypeEnum get(Byte code) {
        for (RpcMessageTypeEnum r : RpcMessageTypeEnum.values()) {
            if (r.code.equals(code)) {
                return r;
            }
        }
        return null;
    }
}
