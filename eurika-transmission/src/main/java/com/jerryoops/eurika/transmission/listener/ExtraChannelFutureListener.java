package com.jerryoops.eurika.transmission.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ExtraChannelFutureListener {

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

//    /**
//     * 返回一个能够重试的ChannelFutureListener
//     * @param maxRetry 最大重试次数
//     * @param delayMillis 每次重试的时间间隔，以毫秒为单位
//     * @param promise 当所有重试都失败时，需要通知的channelPromise。可以为null。
//     * @return
//     */
    public static ChannelFutureListener retryListener(Runnable task, int retriedTimes, int maxRetry, int delayMillis) {
        return future -> {
            if (!future.isSuccess()) {
                if (retriedTimes >= maxRetry) {
                    log.info("Message sending failed, exceeded maximum retry times({})", maxRetry);
                } else {
                    log.info("Message sending failed, attempting to retry after {} milliseconds...({}/{})", delayMillis, retriedTimes+1, maxRetry);
                    executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
                }
            } else {
                log.info("Message sending succeeded({}/{})", retriedTimes, maxRetry);
            }
        };
    }
}
