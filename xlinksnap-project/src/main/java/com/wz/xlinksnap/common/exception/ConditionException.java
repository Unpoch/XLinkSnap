package com.wz.xlinksnap.common.exception;

import lombok.Getter;

/**
 * 异常
 */
@Getter
public class ConditionException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    private String code;//响应状态码

    public ConditionException(String code, String name){
        super(name);
        this.code = code;
    }

    public ConditionException(String name){
        super(name);
        code = "500";
    }

    public void setCode(String code) {
        this.code = code;
    }
}
