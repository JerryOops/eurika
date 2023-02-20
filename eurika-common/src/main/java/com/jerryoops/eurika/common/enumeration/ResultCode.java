package com.jerryoops.eurika.common.enumeration;

import lombok.Getter;

@Getter
public enum ResultCode {
    /**
     * 正常执行
     */
    OK(1000),
    /**
     * 内部系统错误，笼统的错误
     */
    EXCEPTION_SYSTEM_ERROR(2000),
    /**
     * 参数非法或无效
     */
    EXCEPTION_INVALID_PARAM(2001),
    /**
     * 尝试创建一个已在zookeeper中存在的路径
     */
    EXCEPTION_PATH_ALREADY_EXISTS(2002),
    /**
     * 给定的key值在ServiceMap中无法定位到一个bean
     */
    EXCEPTION_BEAN_NOT_FOUND(2003),
    /**
     * 给定的methodName和parameterType在bean中无法定位到一个方法
     */
    EXCEPTION_METHOD_NOT_FOUND(2004)
    ;

    public final Integer code;
    ResultCode(Integer code) {
        this.code = code;
    }
}
