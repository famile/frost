package com.cjsff.transport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author cjsff
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class FrpcRequest {
    /**
     * 请求id
     */
    private String id;
    private String className;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] params;

}
