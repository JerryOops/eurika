package com.jerryoops.eurika.common.enumeration;

import com.jerryoops.eurika.common.tool.serialization.Serializer;
import com.jerryoops.eurika.common.tool.serialization.impl.ProtostuffSerializer;
import lombok.Getter;

@Getter
public enum SerializationProtocolEnum {

    PROTOSTUFF("protostuff", 1, ProtostuffSerializer.INSTANCE);

    /**
     * 名称
     */
    private final String name;
    /**
     * 内部编码
     */
    private final Integer code;
    private final Serializer serializer;

    SerializationProtocolEnum(String name, Integer code, Serializer serializer) {
        this.name = name;
        this.code = code;
        this.serializer = serializer;
    }

    public static boolean isValid(Byte code) {
        return get(code) != null;
    }

    public static Serializer getSerializer(Byte code) {
        SerializationProtocolEnum s = get(code);
        return s == null ? null : s.serializer;
    }

    public static SerializationProtocolEnum get(Byte code) {
        for (SerializationProtocolEnum s : SerializationProtocolEnum.values()) {
            if (s.code.equals((int) code)) {
                return s;
            }
        }
        return null;
    }
}
