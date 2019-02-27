package com.cjsff.client;

import com.cjsff.client.handler.FrpcClientHandler;
import com.cjsff.client.pool.FrpcPooledChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.sf.cglib.beans.BeanCopier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cjsff
 */
public class FrpcClient {

    private static final Logger log = LoggerFactory.getLogger(FrpcClient.class);

    private FrpcClientOption frpcClientOption = new FrpcClientOption();
    private FrpcPooledChannel frpcPooledChannel;
    private Bootstrap bootstrap;

    public FrpcClient(FrpcClientOption option,String zkAddress) {

        frpcPooledChannel = new FrpcPooledChannel(zkAddress, this);
        if (option != null) {
            BeanCopier copier = BeanCopier.create(FrpcClientOption.class, FrpcClientOption.class, false);
            copier.copy(option, frpcClientOption, null);
        }

        EventLoopGroup work;
        bootstrap = new Bootstrap();
        if (Epoll.isAvailable()) {
            work = new EpollEventLoopGroup(frpcClientOption.getNettyWorkThreadNum());
            bootstrap.channel(EpollSocketChannel.class);
            log.info("use epoll edge trigger mode");
        } else {
            work = new NioEventLoopGroup(frpcClientOption.getNettyWorkThreadNum());
            bootstrap.channel(NioSocketChannel.class);
            log.info("use normal mode");
        }
        bootstrap.group(work);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, frpcClientOption.getConnectTimeOutMillis());
        bootstrap.option(ChannelOption.SO_KEEPALIVE, frpcClientOption.isKeepAlive());
        bootstrap.option(ChannelOption.TCP_NODELAY, frpcClientOption.isNoDelay());
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new FrpcClientHandler());
            }
        });

    }

    public Channel getConnect(String address,int port) throws InterruptedException {
        ChannelFuture future = bootstrap.connect(address,port).sync();
        return future.channel();
    }

    public FrpcPooledChannel getFrpcPooledChannel() {
        return frpcPooledChannel;
    }

    public FrpcClientOption getFrpcClientOption() {
        return frpcClientOption;
    }

}


