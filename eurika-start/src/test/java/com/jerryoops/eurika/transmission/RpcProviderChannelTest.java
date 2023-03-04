package com.jerryoops.eurika.transmission;

import cn.hutool.core.bean.BeanUtil;
import com.jerryoops.eurika.Application;
import com.jerryoops.eurika.common.constant.TransmissionConstant;
import com.jerryoops.eurika.common.domain.ConnectionInfo;
import com.jerryoops.eurika.common.domain.ServiceInfo;
import com.jerryoops.eurika.common.enumeration.CompressionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.RpcMessageTypeEnum;
import com.jerryoops.eurika.common.enumeration.SerializationProtocolEnum;
import com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum;
import com.jerryoops.eurika.transmission.domain.RpcMessage;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.handler.ChannelHandlerInitializer;
import com.jerryoops.eurika.transmission.handler.rpc.codec.RpcDecoder;
import com.jerryoops.eurika.transmission.handler.rpc.codec.RpcEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class RpcProviderChannelTest {

    public static void main(String[] args) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setServiceName("serviceName");
        serviceInfo.setHost("123.4.5.6");
        serviceInfo.setPort(123);
        serviceInfo.setGroup("group");
        serviceInfo.setVersion("1.2.3");

        ConnectionInfo connectionInfo = BeanUtil.copyProperties(serviceInfo, ConnectionInfo.class);
        System.out.println(connectionInfo);
    }

    EmbeddedChannel rpcChannel = new EmbeddedChannel(ChannelHandlerInitializer.forProvider(TransmissionProtocolEnum.RPC));

    @Test
    public void testRpcInboundHandlers() {
        ByteBuf buf = this.getBytesForRpcMessage();
        rpcChannel.write(buf);
    }


    private ByteBuf getBytesForRpcMessage() {
        EmbeddedChannel encoderChannel = new EmbeddedChannel(new RpcEncoder());
        EmbeddedChannel decoderChannel = new EmbeddedChannel(new RpcDecoder());

        // prepare pojo
        RpcMessage message = RpcMessage.builder()
                .requestId(12345L)
                .serialization(SerializationProtocolEnum.PROTOSTUFF.getCode())
                .compression(CompressionProtocolEnum.GZIP.getCode())
                .version(TransmissionConstant.RPC_MESSAGE_VERSION)
                .type(RpcMessageTypeEnum.RPC_REQUEST.getCode())
                .magic(TransmissionConstant.RPC_MESSAGE_MAGIC)
                .build();
        RpcRequest request = new RpcRequest();
        request.setRequestId(12345L);
        request.setParameters(new Object[0]);
        request.setParameterTypes(new Class[0]);
        request.setGroup("group1");
        request.setClassName("org.example.testing.service.HelloService");
        request.setMethodName("sayHello");
        request.setVersion("1.0.0");
        message.setBody(request);

        boolean encodeResult = encoderChannel.writeOutbound(message);
        Assert.assertTrue(encodeResult);
        ByteBuf buf = encoderChannel.readOutbound();
        return buf;
    }

}
