package com.jerryoops.eurika.common.constant;

public class TransmissionConstant {
    /*
     * RPC协议常量 (customized length-based protocol)
     */
    /**
     * 最大RPC报文长度（包含头部）。
     */
    public static final Integer RPC_MESSAGE_MAX_LENGTH = 20 * 1024 * 1024;
    /**
     * RPC报文头部的固定长度：24 Bytes。这也是一条RPC报文的最小长度。
     */
    public static final Integer RPC_MESSAGE_HEADER_LENGTH = 24;
    /**
     * 魔数。
     */
    public static final Byte RPC_MESSAGE_MAGIC = (byte) 123;
    /**
     * RPC通讯协议版本。
     */
    public static final Byte RPC_MESSAGE_VERSION = (byte) 1;

}
