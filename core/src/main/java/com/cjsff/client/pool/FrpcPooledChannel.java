package com.cjsff.client.pool;

import com.cjsff.client.FrpcClient;
import com.cjsff.client.FrpcClientOption;
import com.cjsff.registry.RegisterInfo;
import com.cjsff.registry.ServerRegisterDiscovery;
import com.cjsff.registry.ZookeeperService;
import com.cjsff.utils.NetUtils;
import io.netty.channel.Channel;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cjsff
 */
public class FrpcPooledChannel implements FrpcConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(FrpcPooledChannel.class);

    private GenericObjectPool<Channel> channelGenericObjectPool;

    public FrpcPooledChannel(String address, FrpcClient client) {

        FrpcClientOption frpcClientOption = client.getFrpcClientOption();
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxWaitMillis(frpcClientOption.getConnectTimeOutMillis());
        config.setMaxTotal(frpcClientOption.getMaxTotalConnections());
        config.setMaxIdle(frpcClientOption.getMaxTotalConnections());
        config.setMinIdle(frpcClientOption.getMinIdleConnections());
        config.setTestWhileIdle(true);
        config.setTimeBetweenEvictionRunsMillis(frpcClientOption.getTimeBetweenEvictionRunsMillis());

        ServerRegisterDiscovery serverRegisterDiscovery = new ZookeeperService(address);
        String serverAddressAndPort = serverRegisterDiscovery.discovery();
        RegisterInfo registerInfo = NetUtils.getRegisterInfo(serverAddressAndPort);

        channelGenericObjectPool = new GenericObjectPool<Channel>(
                new ChannelPoolFactory(client, registerInfo.getHost(),registerInfo.getPort()), config);
        try {
            channelGenericObjectPool.preparePool();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public Channel getChannel() throws Exception {
        return channelGenericObjectPool.borrowObject();
    }

    @Override
    public void returnChannel(Channel channel) {
        channelGenericObjectPool.returnObject(channel);
    }
}
