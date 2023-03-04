package com.jerryoops.eurika.transmission.functioner;

import com.jerryoops.eurika.common.domain.exception.EurikaException;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * provider端所用对象。接收一个RpcRequest，调用对应方法后，返回一个RpcResponse。
 */
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
    public RpcResponse<?> invoke(RpcRequest request) {
        try {
            Object bean = serviceHolder.getServiceBean(request.getClassName(), request.getGroup(), request.getVersion());
            Method method = bean.getClass().getMethod(request.getMethodName(), request.getParameterTypes());
            Object result = method.invoke(bean, request.getParameters());
            return RpcResponse.build(request.getRequestId(), ResultCode.OK, "OK", result);

        } catch (EurikaException | NoSuchMethodException | IllegalAccessException e) {
            if (e instanceof EurikaException &&
                    !ResultCode.EXCEPTION_INACCESSIBLE_CALL.getCode().equals(((EurikaException)e).getCode())) {
                log.warn("Exception caught: ", e);
            }
            // 无法根据给定的className定位到一个bean ~ invoked by getServiceBean() ~ EurikaException
            // 无法根据给定的methodName定位到bean中的方法 / 被调用方法不可访问。invoked by bean.getClass().getMethod() / method.invoke()
            String errorMessage = "Cannot locate any bean or method that match with given specification, or that they are inaccessible";
            log.warn(errorMessage, e);
            return RpcResponse.build(request.getRequestId(), ResultCode.EXCEPTION_INACCESSIBLE_CALL, errorMessage, null);

        } catch (InvocationTargetException e) {
            // 被调用方法内部抛出异常。invoked by method.invoke()
            String errorMessage = "An exception is thrown by the underlying method that was invoked";
            log.warn(errorMessage, e);
            return RpcResponse.build(request.getRequestId(), ResultCode.EXCEPTION_SYSTEM_ERROR, errorMessage, null);
        } catch (Exception e) {
            log.warn("Exception caught: ", e);
        }
        return RpcResponse.fail(request.getRequestId());
    }


}
