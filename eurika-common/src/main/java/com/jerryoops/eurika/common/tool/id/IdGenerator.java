package com.jerryoops.eurika.common.tool.id;

import cn.hutool.core.util.RandomUtil;

import java.util.concurrent.atomic.AtomicLong;


public class IdGenerator {

    private static final AtomicLong REQUEST_ID = new AtomicLong(RandomUtil.randomLong(Long.MAX_VALUE));

    /**
     * client使用，用于生成某次请求的requestId
     */
    public static Long generateRequestId() {
        return REQUEST_ID.getAndIncrement();
    }
}
