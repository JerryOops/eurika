package com.jerryoops.eurika.common.domain.exception;

import com.jerryoops.eurika.common.enumeration.ResultCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EurikaException extends RuntimeException {

    private static final long serialVersionUID = -4314500395566730671L;
    /**
     * 错误编码
     */
    private Integer code;
    /**
     * 错误信息
     */
    private String msg;
    /**
     * Optional: 自行定义的承载信息
     */
    private Object data;

    private EurikaException() {}

    private EurikaException(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private EurikaException(Integer code, String msg) {
        this(code, msg, null);
    }

    public static EurikaException fail() {
        return new EurikaException(ResultCode.EXCEPTION_SYSTEM_ERROR.getCode(), "System error!");
    }

    public static EurikaException fail(ResultCode code, String msg) {
        return new EurikaException(code.getCode(), msg);
    }

    public static EurikaException fail(ResultCode code, String msg, Object data) {
        return new EurikaException(code.getCode(), msg, data);
    }
}
