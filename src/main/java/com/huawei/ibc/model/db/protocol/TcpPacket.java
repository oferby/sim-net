package com.huawei.ibc.model.db.protocol;

public class TcpPacket extends IpPacket{

    private Short sourcePort;
    private Short destinationPort;

    public Short getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(Short sourcePort) {
        this.sourcePort = sourcePort;
    }

    public Short getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(Short destinationPort) {
        this.destinationPort = destinationPort;
    }
}
