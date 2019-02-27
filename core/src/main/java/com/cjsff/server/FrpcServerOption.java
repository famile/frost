package com.cjsff.server;

import lombok.Getter;
import lombok.Setter;

/**
 * @author cjsff
 */
@Getter
@Setter
public class FrpcServerOption {

    private boolean keepAlive = true;
    private boolean tcpNoDelay = true;
    private int linger = 5;
    private int sendBufferSize = 1024 * 24;
    private int receiveBufferSize = 1024 * 24;
    private int backlog = 1024;

    private int nettyBossThreadNum = Runtime.getRuntime().availableProcessors() * 2;
    private int nettyWorkThreadNum = Runtime.getRuntime().availableProcessors() * 2;

}
