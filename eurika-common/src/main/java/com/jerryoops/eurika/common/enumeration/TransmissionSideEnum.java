package com.jerryoops.eurika.common.enumeration;

import lombok.Getter;

@Getter
public enum TransmissionSideEnum {
    CONSUMER("consumer"),
    PROVIDER("provider")
    ;

    private final String name;
    TransmissionSideEnum(String name) {
        this.name = name;
    }
}
