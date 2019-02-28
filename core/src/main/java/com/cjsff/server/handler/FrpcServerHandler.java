package com.cjsff.server.handler;

import com.cjsff.transport.FrpcRequest;
import com.cjsff.transport.FrpcResponse;
import com.cjsff.transport.JsonSerializer;
import com.cjsff.transport.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;

/**
 * @author cjsff
 */
public class FrpcServerHandler extends SimpleChannelInboundHandler<Object> {


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        int len = buf.readableBytes();

        if (len > 0) {

            byte[] bytes = new byte[len];
            buf.readBytes(bytes);

            // 反序列化
            Serialization json = new JsonSerializer();
            FrpcRequest request = json.deserialize(bytes, FrpcRequest.class);

            // 反射执行请求中要调用的接口
            Object clazz = Class.forName(request.getClassName()).newInstance();
            Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            Object result = method.invoke(clazz, request.getParams());

            // 把结果组装发送到客户端
            FrpcResponse response = new FrpcResponse();
            response.setId(request.getId());
            response.setResult(result.toString());

            byte[] responseByte = json.serialize(response);
            ByteBuf out = ctx.alloc().ioBuffer();
            out.writeBytes(responseByte);
            ctx.channel().writeAndFlush(out);

        }
    }
}
