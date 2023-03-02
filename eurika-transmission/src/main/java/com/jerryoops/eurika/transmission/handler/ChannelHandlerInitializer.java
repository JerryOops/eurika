package com.jerryoops.eurika.transmission.handler;

import com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum;
import com.jerryoops.eurika.transmission.handler.http.initializer.HttpConsumerChannelInitializer;
import com.jerryoops.eurika.transmission.handler.http.initializer.HttpProviderChannelInitializer;
import com.jerryoops.eurika.transmission.handler.rpc.initializer.RpcConsumerChannelInitializer;
import com.jerryoops.eurika.transmission.handler.rpc.initializer.RpcProviderChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import static com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum.HTTP;
import static com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum.RPC;

public class ChannelHandlerInitializer {

    public static <C extends Channel> ChannelInitializer<C> forConsumer(TransmissionProtocolEnum protocol) {
        if (RPC.equals(protocol)) {
            return new RpcConsumerChannelInitializer<>();
        } else if (HTTP.equals(protocol)) {
            return new HttpConsumerChannelInitializer<>();
        }
        // will not happen
        return null;
    }

    public static <C extends Channel> ChannelInitializer<C> forProvider(TransmissionProtocolEnum protocol) {
        if (RPC.equals(protocol)) {
            return new RpcProviderChannelInitializer<>();
        } else if (HTTP.equals(protocol)) {
            return new HttpProviderChannelInitializer<>();
        }
        // will not happen
        return null;
    }
}
