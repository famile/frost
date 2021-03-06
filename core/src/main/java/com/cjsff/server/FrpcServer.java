package com.cjsff.server;

import com.cjsff.registry.ServerRegisterDiscovery;
import com.cjsff.registry.ZookeeperService;
import com.cjsff.server.handler.FrpcServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.sf.cglib.beans.BeanCopier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cjsff
 */
public class FrpcServer {

    private static final Logger log = LoggerFactory.getLogger(FrpcServer.class);

    private FrpcServerOption frpcServerOption = new FrpcServerOption();

    public FrpcServer(int port) throws InterruptedException {
        this(port, null, null);
    }

    public FrpcServer(int port,String zkAddress) throws InterruptedException {
        this(port, zkAddress,null);
    }

    public FrpcServer(int port, String zkAddress,FrpcServerOption option) throws InterruptedException {

        // 判断用户是否设置自定义服务端相关配置
        if (option != null) {
            BeanCopier copier = BeanCopier.create(FrpcServerOption.class, FrpcServerOption.class, false);
            copier.copy(option, frpcServerOption, null);
        }

        EventLoopGroup boss;
        EventLoopGroup work;
        ServerBootstrap bootstrap = new ServerBootstrap();
        // 选择IO模型
        if (Epoll.isAvailable()) {
            boss = new EpollEventLoopGroup(frpcServerOption.getNettyBossThreadNum());
            work = new EpollEventLoopGroup(frpcServerOption.getNettyWorkThreadNum());
            bootstrap.channel(EpollServerSocketChannel.class);
            bootstrap.option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
            bootstrap.childOption(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
            log.info("use epoll edge trigger model.");
        } else {
            boss = new NioEventLoopGroup(frpcServerOption.getNettyBossThreadNum());
            work = new NioEventLoopGroup(frpcServerOption.getNettyWorkThreadNum());
            bootstrap.channel(NioServerSocketChannel.class);
            log.info("use normal model.");
        }
        bootstrap.group(boss, work);
        // 配置TPC相关参数
        bootstrap.option(ChannelOption.SO_BACKLOG, frpcServerOption.getBacklog());
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, frpcServerOption.isKeepAlive());
        bootstrap.childOption(ChannelOption.TCP_NODELAY, frpcServerOption.isTcpNoDelay());
        bootstrap.childOption(ChannelOption.SO_LINGER, frpcServerOption.getLinger());
        bootstrap.childOption(ChannelOption.SO_SNDBUF, frpcServerOption.getSendBufferSize());
        bootstrap.childOption(ChannelOption.SO_RCVBUF, frpcServerOption.getReceiveBufferSize());
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                // 绑定服务端处理器
                ch.pipeline().addLast(new FrpcServerHandler());
            }
        });

        // 绑定端口
        bootstrap.bind(port).sync().addListener(future -> {
            if (future.isSuccess()) {
                log.info("server bind port is success");
                // 端口绑定成功后，判断用户是否要把服务注册到zookeeper
                if (zkAddress != null) {
                    ServerRegisterDiscovery serverRegisterDiscovery = new ZookeeperService(zkAddress);
                    serverRegisterDiscovery.register(port);
                }
            }
        });

    }
}
