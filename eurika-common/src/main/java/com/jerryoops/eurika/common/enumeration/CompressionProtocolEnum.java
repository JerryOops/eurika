package com.jerryoops.eurika.common.enumeration;

import com.jerryoops.eurika.common.tool.compression.Compressor;
import com.jerryoops.eurika.common.tool.compression.impl.GzipCompressor;
import lombok.Getter;

/**
 * 压缩方式枚举
 */
@Getter
public enum CompressionProtocolEnum {

    GZIP("gzip", (byte) 1, GzipCompressor.INSTANCE)
    ;

    /**
     * 名称
     */
    private final String name;
    /**
     * 内部编码
     */
    private final Byte code;

    private final Compressor compressor;

    CompressionProtocolEnum(String name, Byte code, Compressor compressor) {
        this.name = name;
        this.code = code;
        this.compressor = compressor;
    }

    public static Compressor getCompressor(Byte code) {
        CompressionProtocolEnum c = get(code);
        return c == null ? GZIP.compressor : c.compressor;
    }

    public static boolean isValid(Byte code) {
        return get(code) != null;
    }

    public static CompressionProtocolEnum get(Byte code) {
        for (CompressionProtocolEnum c : CompressionProtocolEnum.values()) {
            if (c.code.equals(code)) {
                return c;
            }
        }
        return null;
    }
}
