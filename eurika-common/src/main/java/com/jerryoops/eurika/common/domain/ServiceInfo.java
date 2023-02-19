package com.jerryoops.eurika.common.domain;

import lombok.Data;


/**
 * 用于承载一个provider server提供的一项服务信息的POJO。
 * 对于zookeeper实现的注册中心而言，该POJO与一条路径唯一对应。
 */
@Data
public class ServiceInfo {

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


    /**
     * Provider's IP address (used to receive rpc call)
     */
    private String host;
    /**
     * Provider's port (used to receive rpc call)
     */
    private Integer port;
}
