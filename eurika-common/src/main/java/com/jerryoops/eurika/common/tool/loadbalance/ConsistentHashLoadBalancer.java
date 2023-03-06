package com.jerryoops.eurika.common.tool.loadbalance;

import cn.hutool.core.collection.CollectionUtil;
import com.jerryoops.eurika.common.domain.ConnectionInfo;
import org.apache.curator.shaded.com.google.common.collect.Lists;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Reference:
 * <p>
 *     <a href="https://github.com/apache/dubbo/blob/2d9583adf26a2d8bd6fb646243a9fe80a77e65d5/dubbo-cluster/src/main/java/org/apache/dubbo/rpc/cluster/loadbalance/ConsistentHashLoadBalance.java">dubbo</a>
 *     <a href="https://github.com/Snailclimb/guide-rpc-framework/blob/master/rpc-framework-simple/src/main/java/github/javaguide/loadbalance/loadbalancer/ConsistentHashLoadBalance.java">guide-rpc-framework</a>
 * </p>
 */
public class ConsistentHashLoadBalancer {
    // serviceName --> ConsistentHashSelector instance
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectorMap = new ConcurrentHashMap<>();

    /**
     *
     * @param serviceKey 唯一标识一个service的key值（即：serviceName#group#version）
     * @param selectionKey 使用selectionKey作为被哈希的对象，用于从哈希环中选取一个元素（可使用methodName做此元素）
     * @param list 使用一致性哈希算法，从中选取一个
     * @return
     */
    public ConnectionInfo select(String serviceKey, String selectionKey, List<ConnectionInfo> list) {
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }
        if (list.size() == 1) {
            return Lists.newArrayList(list).get(0);
        }
        // 使用serviceKey，从selectorMap中取出其对应的一致性哈希选择器
        ConsistentHashSelector selector = selectorMap.get(serviceKey);
        int hashId = System.identityHashCode(list);
        // 如果selectorMap中不存在serviceKey对应的selector，
        // 或者selector存在于selectorMap、但是它的标识符(hashId)与本次生成的不一样（说明待选取的列表内容发生了改变，则重新生成一个selector）
        if (selector == null || selector.hashId != hashId) {
            selector = new ConsistentHashSelector(list, 160, hashId);
            selectorMap.put(serviceKey, selector);
        }
        return selector.select(selectionKey);
    }

    private static class ConsistentHashSelector {
        /**
         * 存放所有虚拟节点的treeMap。
         */
        private final TreeMap<Long, ConnectionInfo> virtualInvokerMap;

        /**
         * 本selector实例的唯一哈希标识符。
         */
        private final int hashId;

        /**
         * 构造一个一致性哈希选择器，与invokerList唯一对应。
         * @param invokerList {@link ConsistentHashSelector#select(String)}方法就是从invokers中选出一个。
         * @param replicaNumber 希望为每个invoker构建多少个虚拟节点（virtualInvoker，防止哈希环倾斜）。
         * @param hashId 本{@link ConsistentHashSelector}实例的唯一哈希标识符。
         */
        ConsistentHashSelector(List<ConnectionInfo> invokerList, int replicaNumber, int hashId) {
            this.virtualInvokerMap = new TreeMap<>();
            this.hashId = hashId;

            for (ConnectionInfo invoker : invokerList) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker.getHost() + ":" +  invoker.getPort() + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokerMap.put(m, invoker);
                    }
                }
            }
        }

        /**
         * 使用给定的key值进行哈希映射、得到哈希值，从virtualInvokerMap环之中选出离哈希值最近的entry，返回entry.value
         * （即：返回构造器传入的invokerList中的一个invoker实例）
         * @param key
         * @return
         */
        public ConnectionInfo select(String key) {
            byte[] digest = md5(key);
            return selectForKey(hash(digest, 0));
        }

        private static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            return md.digest();
        }

        private static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        private ConnectionInfo selectForKey(long hashCode) {
            Map.Entry<Long, ConnectionInfo> entry = virtualInvokerMap.tailMap(hashCode, true).firstEntry();
            if (entry == null) {
                entry = virtualInvokerMap.firstEntry();
            }
            return entry.getValue();
        }
    }
}
