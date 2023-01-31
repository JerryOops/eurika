package com.jerryoops.eurika.common.domain.exception;

import com.jerryoops.eurika.common.constant.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BusinessException extends RuntimeException {
    /**
     * 错误编码
     */
    private Integer code;
    /**
     * 错误信息
     */
    private String msg;

    private BusinessException() {}

    private BusinessException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static BusinessException fail() {
        return new BusinessException(ErrorCode.EXCEPTION_SYSTEM_ERROR, "System error!");
    }

    public static BusinessException fail(Integer code, String message) {
        return new BusinessException(code, message);
    }
}
