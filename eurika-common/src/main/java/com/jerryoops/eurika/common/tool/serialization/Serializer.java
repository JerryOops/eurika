package com.jerryoops.eurika.common.tool.serialization;

public interface Serializer {

    <T> byte[] serialize(T obj);

    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
