package com.jerryoops.eurika.common.domain;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * 用于承载连接信息。
 */
@Data
public class ConnectionInfo {
    /**
     * Provider's IP address (used to receive rpc call)
     */
    private String host;
    /**
     * Provider's port (used to receive rpc call)
     */
    private Integer port;
    /**
     * the channel(netty) connected to host:port
     */
    private Channel channel;
}
