package com.example.springboot.pojo;

import org.springframework.http.HttpStatus;

public class ResponseMessage<T>{
    private Integer code;
    private String message;
    private T data;

    public ResponseMessage(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    //接口请求成功
    public static <T>ResponseMessage<T> success(T data) {
        return new ResponseMessage<T>(HttpStatus.OK.value(), "success", data);
        //HttpStatus.OK.value()就是200
    }

    public static <T>ResponseMessage<T> success() {
        return new ResponseMessage<T>(HttpStatus.OK.value(), "success", null);
        //HttpStatus.OK.value()就是200
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
