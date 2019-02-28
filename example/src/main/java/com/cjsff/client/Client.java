package com.cjsff.client;

import com.cjsff.service.SayHelloService;
import com.cjsff.service.impl.SayHelloServiceImpl;

/**
 * @author cjsff
 */
public class Client {

    public static void main(String[] args) {
        FrpcClientOption option = new FrpcClientOption();
        FrpcClient client = new FrpcClient(option,"localhost:2181");
        long startTime = System.currentTimeMillis();
        int requestNum = 100000;
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
