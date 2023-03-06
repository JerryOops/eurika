package com.jerryoops.eurika.common.enumeration;

import lombok.Getter;

@Getter
public enum LoadBalanceEnum {

    CONSISTENT_HASH("hash"),

    RANDOM("random")
    ;

    private final String name;

    LoadBalanceEnum(String name) {
        this.name = name;
    }

    public static LoadBalanceEnum getByName(String name) {
        for (LoadBalanceEnum l : LoadBalanceEnum.values()) {
            if (l.name.equals(name)) {
                return l;
            }
        }
        return LoadBalanceEnum.RANDOM;
    }
}
