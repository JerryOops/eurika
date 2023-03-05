package com.jerryoops.eurika.common.enumeration;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

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
     * 尝试创建一个已在zookeeper中存在的路径(registry)
     */
    EXCEPTION_PATH_ALREADY_EXISTS(2002),
    /**
     * 给定的key值在ServiceBeanMap中无法定位到一个bean, 或者给定的methodName和parameterType在bean中无法定位到一个方法，或者被调用方法不可达
     */
    EXCEPTION_INACCESSIBLE_CALL(2003),
    /**
     * 给定的class json无法转为某个Class类对象
     */
    EXCEPTION_CLASS_NOT_FOUND(2004),
    /**
     * channel不可用
     */
    EXCEPTION_CHANNEL_UNAVAILABLE(2005)
    ;

    // fields
    public final Integer code;


    // 存放ResultCode到HttpResponseStatus的映射
    private static final Map<ResultCode, HttpResponseStatus> httpResponseStatusMap;
    // 存放code(Integer)到ResultCode的映射
    private static final Map<Integer, ResultCode> codeMap;

    static {
        // init httpResponseStatusMao
        httpResponseStatusMap = new HashMap<>(ResultCode.values().length);
        httpResponseStatusMap.put(ResultCode.OK, HttpResponseStatus.OK);
        httpResponseStatusMap.put(ResultCode.EXCEPTION_SYSTEM_ERROR, HttpResponseStatus.SERVICE_UNAVAILABLE);
        httpResponseStatusMap.put(ResultCode.EXCEPTION_INVALID_PARAM, HttpResponseStatus.BAD_REQUEST);
        httpResponseStatusMap.put(ResultCode.EXCEPTION_INACCESSIBLE_CALL, HttpResponseStatus.BAD_REQUEST);
        httpResponseStatusMap.put(ResultCode.EXCEPTION_CLASS_NOT_FOUND, HttpResponseStatus.BAD_REQUEST);
        // init codeMap
        codeMap = new HashMap<>(ResultCode.values().length);
        for (ResultCode resultCode : ResultCode.values()) {
            codeMap.put(resultCode.getCode(), resultCode);
        }
    }

    // constructor
    ResultCode(Integer code) {
        this.code = code;
    }

    // static methods
    public static HttpResponseStatus mapHttp(ResultCode resultCode) {
        return httpResponseStatusMap.getOrDefault(resultCode, HttpResponseStatus.OK);
    }

    public static HttpResponseStatus mapHttp(Integer code) {
        return mapHttp(codeMap.get(code));
    }
}
