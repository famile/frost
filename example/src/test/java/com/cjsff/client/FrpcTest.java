package com.cjsff.client;

import com.cjsff.server.FrpcServer;
import com.cjsff.service.SayHelloService;
import com.cjsff.service.impl.SayHelloServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.InetSocketAddress;

@RunWith(JUnit4.class)
public class FrpcTest {

    @Before
    public void startServer() throws InterruptedException {
        FrpcServer server = new FrpcServer(10027);
    }

    @Test
    public void startClient() {
        FrpcClientOption option = new FrpcClientOption();
        InetSocketAddress serverAddress = new InetSocketAddress("localhost", 10027);
        FrpcClient client = new FrpcClient(serverAddress,option);
        long startTime = System.currentTimeMillis();
        int requestNum = 1;
        for (int i = 0; i < requestNum; i++) {
            SayHelloService sayHelloService = FrpcProxy.getProxy(SayHelloServiceImpl.class, client);
            System.out.println(sayHelloService.sayHello("cjsff"));
        }
        long spendTime = (System.currentTimeMillis() - startTime)/1000;
        System.out.println(String.format("spend-total-time:%ss,req/s=%s",
                spendTime,(
                        (double)requestNum/spendTime
                )
                )
        );
    }
}