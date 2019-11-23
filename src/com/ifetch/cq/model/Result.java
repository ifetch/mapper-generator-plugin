package com.ifetch.cq.model;

/**
 * Created by cq on 19-11-22.
 */
public class Result<T> {
    private boolean success;

    private String desc;

    private T t;

    public Result(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public Result setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public Result setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public T getT() {
        return t;
    }

    public Result<T> setT(T t) {
        this.t = t;
        return this;
    }

}
