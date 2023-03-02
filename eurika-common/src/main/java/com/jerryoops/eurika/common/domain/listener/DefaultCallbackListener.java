package com.jerryoops.eurika.common.domain.listener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultCallbackListener implements CallbackListener {

    @Override
    public void onExecutionCompleted(Object result) {
        log.info("Async call execution completed, result = {}", result);
    }

    @Override
    public void onThrowableCaught(Throwable th) {
        log.info("Async call throwable caught, throwable = ", th);
    }
}
