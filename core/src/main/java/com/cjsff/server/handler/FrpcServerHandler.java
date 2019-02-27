package com.cjsff.server.handler;

import com.cjsff.transport.FrpcRequest;
import com.cjsff.transport.FrpcResponse;
import com.cjsff.transport.JsonSerializer;
import com.cjsff.transport.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author cjsff
 */
public class FrpcServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger log = LoggerFactory.getLogger(FrpcServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        int len = buf.readableBytes();

        if (len > 0) {

            byte[] bytes = new byte[len];
            buf.readBytes(bytes);

            Serialization json = new JsonSerializer();
            FrpcRequest request = json.deserialize(bytes, FrpcRequest.class);

            Object clazz = Class.forName(request.getClassName()).newInstance();
            Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object result = method.invoke(clazz, request.getParams());

            FrpcResponse response = new FrpcResponse();
            response.setId(request.getId());
            response.setResult(result.toString());

            byte[] bytes1 = json.serialize(response);
            ByteBuf out = ctx.alloc().ioBuffer();
            out.writeBytes(bytes1);
            ctx.channel().writeAndFlush(out);

        }
    }
}
