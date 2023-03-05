package com.jerryoops.eurika.transmission.functioner;

import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.domain.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于持有那些已经被consumer发送、但是尚未得到provider相应的请求结果。
 */
@Slf4j
@Component
public class UnrespondedFutureHolder {

    private final Map<Long, CompletableFuture<RpcResponse<?>>> unrespondedFutureMap = new ConcurrentHashMap<>();

    public void put(Long requestId, CompletableFuture<RpcResponse<?>> future) {
        // 如果map中已有requestId的键值对，将会导致覆盖
        unrespondedFutureMap.put(requestId, future);
    }

    public void complete(RpcResponse<?> response) {
        CompletableFuture<RpcResponse<?>> future = unrespondedFutureMap.remove(response.getRequestId());
        if (null == future || future.isDone()) {
            log.warn("Invalid requestId [{}] or status of future [{}]", response.getRequestId(), future);
            return;
        }
        future.complete(response);
    }

    public void completeExceptionally(Long requestId, Throwable ex) {
        CompletableFuture<RpcResponse<?>> future = unrespondedFutureMap.remove(requestId);
        if (null == future || future.isDone()) {
            log.warn("Attempting to remove a non-existed future with requestId = {}, " +
                    "or that the future removed [{}] is done", requestId, future);
            return;
        }
        future.completeExceptionally(ex);
    }
}
