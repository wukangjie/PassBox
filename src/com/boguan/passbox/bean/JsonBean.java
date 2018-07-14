package com.boguan.passbox.bean;

import java.util.List;

/**
 * Created by wukangjie on 16/12/13.
 */

public class JsonBean {

    private String icon;
    private String host;
    private String color;
    private List<String> key;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<String> getKey() {
        return key;
    }

    public void setKey(List<String> key) {
        this.key = key;
    }
}
