package com.jerryoops.eurika.transmission;

import com.jerryoops.eurika.Application;
import com.jerryoops.eurika.common.enumeration.CompressionProtocolEnum;
import com.jerryoops.eurika.common.enumeration.ResultCode;
import com.jerryoops.eurika.common.enumeration.SerializationProtocolEnum;
import com.jerryoops.eurika.common.enumeration.TransmissionProtocolEnum;
import com.jerryoops.eurika.common.tool.compression.Compressor;
import com.jerryoops.eurika.common.tool.serialization.Serializer;
import com.jerryoops.eurika.common.util.NettyUtil;
import com.jerryoops.eurika.transmission.domain.RpcRequest;
import com.jerryoops.eurika.transmission.handler.ChannelHandlerInitializer;
import com.jerryoops.eurika.transmission.handler.http.HttpRequestDistiller;
import com.jerryoops.eurika.transmission.handler.shared.ProviderInvocationHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class})
public class HttpProviderChannelTest {

    @Test
    public void testHttpInboundHandlers() {
        EmbeddedChannel httpChannel = new EmbeddedChannel(new HttpRequestDistiller(), new ProviderInvocationHandler());

        // RpcRequest POJO
        RpcRequest request = new RpcRequest();
        request.setRequestId(12345L);
        request.setParameters(new Object[]{"JerryOops"});
        request.setParameterTypes(new Class[]{String.class});
        request.setClassName("com.org.instance2.service.HelloService");
        request.setMethodName("hello");
        request.setGroup("DEFAULT_GROUP");
        request.setVersion("1.0.0");
        // serialize
        Serializer serializer = SerializationProtocolEnum.PROTOSTUFF.getSerializer(); // TODO: 2023/3/5 修改为配置
        byte[] bytes = serializer.serialize(request);
        // compress
        Compressor compressor = CompressionProtocolEnum.GZIP.getCompressor();
        bytes = compressor.compress(bytes);
        HttpRequest httpRequest = NettyUtil.buildHttpRequest(bytes, HttpMethod.POST);

        httpChannel.writeInbound(httpRequest);
        Object o = httpChannel.readOutbound();
        System.out.println(o);
    }
}
