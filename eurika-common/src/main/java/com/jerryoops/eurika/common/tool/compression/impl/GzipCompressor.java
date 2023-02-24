package com.jerryoops.eurika.common.tool.compression.impl;

import com.jerryoops.eurika.common.tool.compression.Compressor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompressor implements Compressor {

    private GzipCompressor() {}

    public static final GzipCompressor INSTANCE = new GzipCompressor();

    private static final int BUFFER_SIZE = 4 * 1024;

    @Override
    public byte[] compress(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip compression error", e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             GZIPInputStream gunzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while ((n = gunzip.read(buffer)) > -1) {
                out.write(buffer, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("gzip decompression error", e);
        }
    }
}
