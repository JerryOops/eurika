package com.jerryoops.eurika.common.tool.loadbalance;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.jerryoops.eurika.common.domain.ConnectionInfo;
import org.apache.curator.shaded.com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

public class RandomLoadBalancer {

    public ConnectionInfo select(List<ConnectionInfo> list) {
        if (CollectionUtil.isEmpty(list)) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        return list.get(RandomUtil.randomInt(list.size()));
    }

}
