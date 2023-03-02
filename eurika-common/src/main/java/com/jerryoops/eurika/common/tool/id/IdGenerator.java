package com.jerryoops.eurika.common.tool.id;

import cn.hutool.core.util.RandomUtil;

import java.util.concurrent.atomic.AtomicLong;


/**
 * 用于生成ID。
 */
public class IdGenerator {

    private static final AtomicLong REQUEST_ID = new AtomicLong(RandomUtil.randomLong(Long.MAX_VALUE));

    /**
     * RPC调用方使用，用于生成某次RPC请求的requestId。
     */
    public static Long generateRequestId() {
        return REQUEST_ID.getAndIncrement();
    }
}
