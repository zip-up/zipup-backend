package com.zipup.server.global.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseException extends RuntimeException {

    private int code;
    private String message;
    private CustomErrorCode status;

    public BaseException(CustomErrorCode status) {
        this.status = status;
    }

}
