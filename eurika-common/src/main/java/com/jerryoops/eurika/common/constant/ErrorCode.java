package com.jerryoops.eurika.common.constant;

public class ErrorCode {
    /**
     * 内部系统错误，笼统的错误
     */
    public static final Integer EXCEPTION_SYSTEM_ERROR = 100;
    /**
     * 参数非法或无效
     */
    public static final Integer EXCEPTION_INVALID_PARAM = 101;
    /**
     * 无法连接到zookeeper
     */
    public static final Integer EXCEPTION_ZOOKEEPER_CONNECTION_FAILED = 102;
    /**
     * 尝试创建一个已在zookeeper中存在的路径
     */
    public static final Integer EXCEPTION_PATH_ALREADY_EXISTS = 103;

}
