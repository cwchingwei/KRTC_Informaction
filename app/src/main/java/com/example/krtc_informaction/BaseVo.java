package com.example.krtc_informaction;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BaseVo<T> implements Serializable {

    @SerializedName("code")
    protected int code = 0;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @SerializedName("success")
    protected boolean success = false;

    @SerializedName("message")
    protected String message = "";

    @SerializedName("id")
    protected String id = "";

    @SerializedName("data")
    protected T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
