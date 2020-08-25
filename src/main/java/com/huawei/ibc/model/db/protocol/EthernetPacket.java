package com.huawei.ibc.model.db.protocol;

public class EthernetPacket {

    private boolean ack = false;
    protected MACAddress sourceMac;
    protected MACAddress destinationMac;
    private short ttl = 10;

    public MACAddress getSourceMac() {
        return sourceMac;
    }

    public void setSourceMac(MACAddress sourceMac) {
        this.sourceMac = sourceMac;
    }

    public MACAddress getDestinationMac() {
        return destinationMac;
    }

    public void setDestinationMac(MACAddress destinationMac) {
        this.destinationMac = destinationMac;
    }

    public short getTtl() {
        return ttl;
    }

    public void setTtl() {
        this.ttl-- ;
        if (this.ttl == 0)
            throw new RuntimeException("TTL expired");
    }

    public void setAck(){
        this.ack = true;
    }

    public boolean isAck() {
        return ack;
    }

    @Override
    public String toString() {
        return "EthernetPacket{" +
                "sourceMac=" + sourceMac +
                ", destinationMac=" + destinationMac +
                ", ttl=" + ttl +
                '}';
    }
}
