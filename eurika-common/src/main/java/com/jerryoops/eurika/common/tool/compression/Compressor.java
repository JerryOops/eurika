package com.jerryoops.eurika.common.tool.compression;

public interface Compressor {
    /**
     * 压缩
     */
    byte[] compress(byte[] bytes);

    /**
     * 解压缩
     */
    byte[] decompress(byte[] bytes);
}
