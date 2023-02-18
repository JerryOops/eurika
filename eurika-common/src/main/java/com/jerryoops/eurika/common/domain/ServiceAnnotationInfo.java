package com.jerryoops.eurika.common.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 用于承载一个被@EurikaService注解标识的service bean的信息，是与@EurikaService注解的各项属性一一对应的POJO。
 * 作为ServiceInfo的一部分，被写入到注册中心中。
 */
@Getter
@Builder
@ToString
public class ServiceAnnotationInfo {
    /**
     * Full name of the service class to be registered, namely the class annotated with @EurikaService.<p>
     * e.g. com.jerryoops.FooService
     */
    private String serviceName;
    /**
     * Value specified by 'group' in the annotation of @EurikaService.
     */
    private String group;
    /**
     * Value specified by 'version' in the annotation of @EurikaService.
     */
    private String version;
}
