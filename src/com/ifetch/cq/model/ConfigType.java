package com.ifetch.cq.model;

/**
 * Created by cq on 19-10-30.
 */
public enum ConfigType {
    connection("连接配置"),
    config("配置文件");

    ConfigType(String desc) {
        this.desc = desc;
    }

    private String desc;

    public String getDesc() {
        return desc;
    }
}
