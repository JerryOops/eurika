package com.jerryoops.eurika.transmission.domain;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.util.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

@Data
@Slf4j
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
                StringUtils.isBlank(request.getMethodName()) || null == request.getParameterTypes() || null == request.getParameters() ||
                null == request.getVersion() || null == request.getGroup()) {
            throw EurikaException.fail(ResultCode.EXCEPTION_INVALID_PARAM, "RpcRequest with invalid parameter(s): " + request,
                    (null == request) ? null : request.getRequestId());
        }
    }

    /**
     * 用于将json字符串转变为RpcRequest对象。
     * 这一过程不能够直接调用JsonUtil.fromJson()，因为parameters中的每个元素的类型是一一对应地定义在parameterTypes之中的。
     * @param json
     * @return
     */
    public static RpcRequest parseJson(String json) {
        JsonObject requestJsonObject = JsonParser.parseString(json).getAsJsonObject();
        JsonArray parameterTypesJsonArray = Optional.ofNullable(requestJsonObject.get("parameterTypes")).map(JsonElement::getAsJsonArray).orElse(null);
        JsonArray parametersJsonArray = Optional.ofNullable(requestJsonObject.get("parameters")).map(JsonElement::getAsJsonArray).orElse(null);
        // RpcRequest, without field parameterTypes and parameters
        requestJsonObject.remove("parameterTypes");
        requestJsonObject.remove("parameters");
        RpcRequest request = JsonUtil.fromJson(requestJsonObject, RpcRequest.class);
        if (null == parameterTypesJsonArray || null == parametersJsonArray) {
            // JSON中不存在 parameterTypes / parameters
            throw EurikaException.fail(ResultCode.EXCEPTION_INVALID_PARAM,
                    "'parameterTypes' or 'parameters' not presented", request.getRequestId());
        }
        // Manually create the two fields
        int size = parameterTypesJsonArray.size();
        if (size != parametersJsonArray.size()) {
            throw EurikaException.fail(ResultCode.EXCEPTION_INVALID_PARAM,
                    "Count mismatch found between 'parameterTypes' and 'parameters'", request.getRequestId());
        }
        Class<?>[] parameterTypes = new Class[size];
        Object[] parameters = new Object[size];
        int i = 0;
        try {
            for (; i < size; i ++) {
                parameterTypes[i] = JsonUtil.fromJson(parameterTypesJsonArray.get(i), Class.class);
                parameters[i] = JsonUtil.fromJson(parametersJsonArray.get(i), parameterTypes[i]);
            }
        } catch (Exception e) {
            if (e.getCause() instanceof ClassNotFoundException) {
                throw EurikaException.fail(ResultCode.EXCEPTION_CLASS_NOT_FOUND,
                        "ClassNotFound exception caught during processing " + parameterTypesJsonArray.get(i),
                        request.getRequestId());
            } else {
                throw e;
            }
        }
        request.setParameterTypes(parameterTypes);
        request.setParameters(parameters);
        return request;
    }
}
