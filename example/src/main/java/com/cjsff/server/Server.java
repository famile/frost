package com.cjsff.server;

import java.net.InetSocketAddress;

public class Server {

    public static void main(String[] args) throws InterruptedException {
        FrpcServer server = new FrpcServer(10027,"localhost:2181");
    }

}
