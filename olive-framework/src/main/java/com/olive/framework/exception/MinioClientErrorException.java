package com.olive.framework.exception;

public class MinioClientErrorException extends RuntimeException{
    public MinioClientErrorException(String msg){
        super(msg);
    }
}
