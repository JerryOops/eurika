package com.jerryoops.eurika.common.util;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

public class JsonUtil {
    private static final Gson gson = new Gson();

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * 当obj本身是非泛型对象时使用(必须满足obj.getClass() != java.lang.class，即obj本身的类型必须可推断)。
     * 注意在满足以上条件的前提下，当obj持有泛型域时，这个方法是可用的。
     */
    public static <T> String toJson(T obj) {
        return gson.toJson(obj);
    }
}
