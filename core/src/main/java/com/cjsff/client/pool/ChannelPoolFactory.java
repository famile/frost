package com.cjsff.client.pool;

import com.cjsff.client.FrpcClient;
import io.netty.channel.Channel;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @author cjsff
 */
public class ChannelPoolFactory extends BasePooledObjectFactory<Channel> {

    private FrpcClient frpcClient;
    private String address;
    private int port;

    public ChannelPoolFactory(FrpcClient frpcClient, String address,int port) {
        this.frpcClient = frpcClient;
        this.address = address;
        this.port = port;
    }

    @Override
    public Channel create() throws Exception {
        return frpcClient.getConnect(address,port);
    }

    @Override
    public PooledObject<Channel> wrap(Channel obj) {
        return new DefaultPooledObject<>(obj);
    }


    @Override
    public void destroyObject(PooledObject<Channel> p) throws Exception {
        Channel channel = p.getObject();
        if (channel != null && channel.isOpen() && channel.isActive()) {
            channel.close();
        }
    }

    @Override
    public boolean validateObject(PooledObject<Channel> p) {
        Channel channel = p.getObject();
        return channel != null && channel.isActive() && channel.isOpen();
    }

}
