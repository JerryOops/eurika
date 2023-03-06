package com.jerryoops.eurika.common.tool.loadbalance;

public abstract class LoadBalancer {

    /**
     * 一致性哈希
     */
    public static final ConsistentHashLoadBalancer CONSISTENT_HASH = new ConsistentHashLoadBalancer();
    /**
     * 随机
     */
    public static final RandomLoadBalancer RANDOM = new RandomLoadBalancer();

}
