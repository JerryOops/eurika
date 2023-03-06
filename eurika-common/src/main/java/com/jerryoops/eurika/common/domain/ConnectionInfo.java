package com.jerryoops.eurika.common.domain;

import io.netty.channel.Channel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

/**
 * 用于承载连接信息。
 */
@Setter
@Getter
@ToString
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

    @Override
    public boolean equals(Object obj) {
        return (ConnectionInfo.class.equals(obj.getClass())) &&
                Objects.equals(this.host, ((ConnectionInfo) obj).getHost()) &&
                Objects.equals(this.port, ((ConnectionInfo) obj).getPort());
    }
}
