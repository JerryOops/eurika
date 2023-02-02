package com.jerryoops.eurika.common.util;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {
    private static final Gson gson = new Gson();

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> String toJson(T obj) {
        return gson.toJson(obj);
    }
}
