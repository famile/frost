package com.cjsff.client.handler;

import com.cjsff.client.FrpcFuture;
import com.cjsff.client.pool.FrpcPooledChannel;
import com.cjsff.transport.FrpcRequest;
import com.cjsff.transport.FrpcResponse;
import com.cjsff.transport.JsonSerializer;
import com.cjsff.transport.Serialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author cjsff
 */
public class FrpcClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger log = LoggerFactory.getLogger(FrpcClientHandler.class);

    private static ConcurrentHashMap<String, FrpcFuture> pendingRpc = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        Serialization jsonSerializer = new JsonSerializer();
        FrpcResponse response = jsonSerializer.deserialize(bytes, FrpcResponse.class);

        String requestId = response.getId();
        FrpcFuture frpcFuture = pendingRpc.get(requestId);
        System.out.println(pendingRpc.get(requestId));
        if (frpcFuture != null) {
            pendingRpc.remove(requestId);
            frpcFuture.done(response);
        }

    }

    public FrpcFuture send(FrpcRequest request,FrpcPooledChannel frpcPooledChannel) throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        FrpcFuture frpcFuture = new FrpcFuture(request);
        pendingRpc.put(request.getId(), frpcFuture);

        Channel channel = frpcPooledChannel.getChannel();
        Serialization json = new JsonSerializer();
        byte[] bytes = json.serialize(request);
        ByteBuf buf = channel.alloc().ioBuffer();
        buf.writeBytes(bytes);
        channel.writeAndFlush(buf).addListener((ChannelFutureListener) future -> latch.countDown());

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        frpcPooledChannel.returnChannel(channel);

        return frpcFuture;
    }

}
