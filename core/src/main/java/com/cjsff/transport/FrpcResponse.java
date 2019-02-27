package com.cjsff.transport;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author cjsff
 */
@Getter
@Setter
@ToString
public class FrpcResponse {

    private String id;
    private String result;
    private String error;

    public boolean isError() {
        return error != null;
    }
}
