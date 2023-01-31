package com.jerryoops.eurika.common.domain;

import lombok.Data;

import java.util.Set;

/**
 * 在serviceMap中与interfaceName为KV关系
 */
@Data
public class ImplementationDO {
    /**
     * 实现类的版本号，@EurikaService(version之值)
     */
    private String version;
    /**
     * 接口方法名称集合
     */
    private Set<String> methodNameSet;
}
