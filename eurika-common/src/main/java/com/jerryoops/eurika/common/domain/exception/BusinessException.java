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
    private String message;

    private BusinessException() {}

    private BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static BusinessException fail() {
        return new BusinessException(ErrorCode.EXCEPTION_SYSTEM_ERROR, "System error!");
    }

    public static BusinessException fail(Integer code, String message) {
        return new BusinessException(code, message);
    }
}
