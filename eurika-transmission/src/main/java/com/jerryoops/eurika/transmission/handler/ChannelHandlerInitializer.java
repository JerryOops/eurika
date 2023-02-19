package com.jerryoops.eurika.transmission.handler;

import com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.TransmissionSideEnum;
import com.jerryoops.eurika.transmission.handler.http.initializer.HttpConsumerChannelInitializer;
import com.jerryoops.eurika.transmission.handler.http.initializer.HttpProviderChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

import static com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum.HTTP;
import static com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum.RPC;
import static com.jerryoops.eurika.common.enumeration.TransmissionSideEnum.CONSUMER;
import static com.jerryoops.eurika.common.enumeration.TransmissionSideEnum.PROVIDER;

public class ChannelHandlerInitializer {

    public static <C extends Channel> ChannelInitializer<C> get(TransmissionProtocolEnum protocol,
                                                         TransmissionSideEnum side) {
        if (CONSUMER.equals(side)) {
            if (RPC.equals(protocol)) {
                return null; // TODO: 2023/2/19 to be fulfilled
            } else if (HTTP.equals(protocol)) {
                return new HttpConsumerChannelInitializer<>();
            }
        } else if (PROVIDER.equals(side)) {
            if (RPC.equals(protocol)) {
                return null; // TODO: 2023/2/19 to be fulfilled
            } else if (HTTP.equals(protocol)) {
                return new HttpProviderChannelInitializer<>();
            }
        }
        // will not happen
        return null;
    }
}
