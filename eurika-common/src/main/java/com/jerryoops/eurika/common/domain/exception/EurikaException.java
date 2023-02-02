package com.jerryoops.eurika.common.domain.exception;

import com.jerryoops.eurika.common.constant.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EurikaException extends RuntimeException {
    /**
     * 错误编码
     */
    private Integer code;
    /**
     * 错误信息
     */
    private String msg;

    private EurikaException() {}

    private EurikaException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static EurikaException fail() {
        return new EurikaException(ErrorCode.EXCEPTION_SYSTEM_ERROR, "System error!");
    }

    public static EurikaException fail(Integer code, String message) {
        return new EurikaException(code, message);
    }
}
