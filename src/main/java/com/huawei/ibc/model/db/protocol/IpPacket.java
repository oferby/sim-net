package com.huawei.ibc.model.db.protocol;

public class IpPacket extends EthernetPacket {

    protected String sourceIp;
    protected String destinationIp;

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }
}
