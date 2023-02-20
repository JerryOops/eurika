package com.jerryoops.eurika.transmission.functioner;

import com.jerryoops.eurika.common.constant.ProviderConstant;
import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.util.StringEscapeUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


@Slf4j
@Component
public class ServiceInvoker {

    @Autowired
    ServiceHolder serviceHolder;

    /**
     * <p>根据RpcRequest指定的信息，使用反射调用指定的方法，并将结果包装为RpcResponse后返回。</p>
     * @param request
     * @return
     */
    public RpcResponse invoke(RpcRequest request) {
        try {
            String key = this.getKeyForServiceMap(request);
            Object bean = serviceHolder.getServiceObject(key);
            Method method = bean.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            Object result = method.invoke(bean, request.getParameters());
            return this.buildRpcResponse(request, result);
        } catch (EurikaException e) {
            // 无法根据给定的className定位到一个bean。invoked by getKeyForServiceMap()
            if (ResultCode.EXCEPTION_BEAN_NOT_FOUND.getCode().equals(e.getCode())) {
                String errorMessage = "[RequestId = " + request.getRequestId() + "] No bean has name that matches with [" + request.getClassName() + "]";
                log.warn(errorMessage, e);
                return RpcResponse.builder()
                        .requestId(request.getRequestId())
                        .code(ResultCode.EXCEPTION_BEAN_NOT_FOUND.getCode())
                        .msg(errorMessage).build();
            }
            log.error("Exception caught: ", e);

        } catch (NoSuchMethodException | IllegalAccessException e) {
            // 无法根据给定的methodName定位到bean中的方法 / 被调用方法不可访问。invoked by bean.getClass().getMethod() / method.invoke()
            String errorMessage = "No method matches with the specification of [methodName = " +  request.getMethodName() +
                    ", parameterTypes = " + Arrays.toString(request.getParameterTypes()) + "] or that it is inaccessible";
            log.warn(errorMessage, e);
            return RpcResponse.builder()
                    .requestId(request.getRequestId())
                    .code(ResultCode.EXCEPTION_METHOD_NOT_FOUND.getCode())
                    .msg(errorMessage).build();

        } catch (InvocationTargetException e) {
            // 被调用方法内部抛出异常。invoked by method.invoke()
            log.warn("An exception is thrown by the method invoked: ", e);
            return RpcResponse.builder()
                    .requestId(request.getRequestId())
                    .code(ResultCode.EXCEPTION_SYSTEM_ERROR.getCode())
                    .msg("An exception is thrown by the underlying method that was invoked").build();
        } catch (Exception e) {
            log.warn("Exception caught: ", e);
            return RpcResponse.FAIL(request.getRequestId());
        }
        return null;
    }

    private RpcResponse buildRpcResponse(RpcRequest request, Object result) {
        return RpcResponse.builder()
                .requestId(request.getRequestId())
                .code(ResultCode.OK.getCode())
                .msg("OK")
                .result(result)
                .resultType(result.getClass())
                .build();
    }

    private String getKeyForServiceMap(RpcRequest request) {
        return StringEscapeUtil.unescape(request.getMethodName()) + ProviderConstant.SERVICE_MAP_KEY_SEPARATOR +
                StringEscapeUtil.unescape(request.getGroup()) + ProviderConstant.SERVICE_MAP_KEY_SEPARATOR +
                StringEscapeUtil.unescape(request.getVersion());
    }
}
