package com.huawei.ibc.model.common;

public class ConnectionMessage {

    private String source;
    private String destination;

    public ConnectionMessage() {
    }

    public ConnectionMessage(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
