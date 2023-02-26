package com.jerryoops.eurika.transmission.domain;

import com.jerryoops.eurika.common.enumeration.ResultCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
@Getter
@Setter
@ToString
public class RpcResponse<T> implements Serializable {

    private RpcResponse() {}

    private static final long serialVersionUID = -5216600002568584265L;
    /**
     * 唯一对应RPC consumer单次调用的UUID值。
     */
    private Long requestId;
    /**
     * 响应编码。
     */
    private Integer code;
    /**
     * 响应信息。
     */
    private String msg;
    /**
     * 响应结果。
     */
    private T result;



    public static <R> RpcResponse<R> build(Long requestId, ResultCode code, String msg, R result) {
        RpcResponse<R> rpcResponse = new RpcResponse<>();
        rpcResponse.setRequestId(requestId);
        rpcResponse.setCode(code.getCode());
        rpcResponse.setMsg(msg);
        rpcResponse.setResult(result);
        return rpcResponse;
    }

    /**
     * 用于构建一个笼统的错误信息RpcResponse对象。
     * @param requestId
     * @return
     */
    public static RpcResponse<?> fail(Long requestId) {
        return RpcResponse.build(requestId, ResultCode.EXCEPTION_SYSTEM_ERROR, "System error", null);
    }


}
