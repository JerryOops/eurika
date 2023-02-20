package com.jerryoops.eurika.transmission.domain;

import com.jerryoops.eurika.common.enumeration.ResultCode;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
@Getter
@ToString
@Builder
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = -5216600002568584265L;
    /**
     * 唯一对应RPC consumer单次调用的UUID值。
     */
    private String requestId;
    /**
     * 响应编码。
     */
    private Integer code;
    /**
     * 响应信息。
     */
    private String msg;
    /**
     * 响应结果类型。
     */
    private Class<?> resultType;
    /**
     * 响应结果。
     */
    private Object result;


    /**
     * 用于构建一个笼统的错误信息RpcResponse对象。
     * @param requestId
     * @return
     */
    public static RpcResponse FAIL(String requestId) {
        return RpcResponse.builder()
                .requestId(requestId)
                .code(ResultCode.EXCEPTION_SYSTEM_ERROR.getCode())
                .msg("System error").build();
    }
}
