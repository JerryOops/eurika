package com.jerryoops.eurika.transmission.domain;

import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = -5681633104494725368L;
    /**
     * 唯一对应RPC consumer单次调用的UUID值。
     */
    private String requestId;
    /**
     * <p>被调用类的全限定名。如果该类没有实现任何接口，则使用该类的全限定名；否则使用该类的唯一直接实现的接口之全限定名。</p>
     * <p>e.g. "com.jerryoops.foo.bar.HelloService.hello"</p>
     *
     */
    private String className;
    /**
     * 被调用方法的名称。
     */
    private String methodName;
    /**
     * 被调用方法的参数类型数组。如果该方法为无参方法，则此参数为非null空数组。
     */
    private Class<?>[] parameterTypes;
    /**
     * 被调用方法的参数值数组。如果该方法为无参方法，则此参数为非null空数组。
     */
    private Object[] parameters;
    /**
     * 被调用方法的@EurikaService标注属性version需与此变量一致。如无法找到匹配的方法，那么将会返回错误信息。
     */
    private String version;
    /**
     * 被调用方法的@EurikaService标注属性group需与此变量一致。如无法找到匹配的方法，那么将会返回错误信息。
     */
    private String group;


    /**
     * 用于检验入参合法性。本方法中抛出的异常将会在handler.exceptionCaught方法中得到处理。
     * @param request
     */
    public static void checkValidity(RpcRequest request) {
        if (null == request || StringUtils.isBlank(request.getRequestId()) || StringUtils.isBlank(request.getClassName()) ||
                StringUtils.isBlank(request.getMethodName()) || null == request.getVersion() || null == request.getGroup()) {
            throw EurikaException.fail(ResultCode.EXCEPTION_INVALID_PARAM, "RpcRequest with invalid parameter(s): " + request,
                    (null == request) ? null : request.getRequestId());
        }
    }
}
