package com.huawei.ibc.model.db.protocol;

public class MplsPacket extends EthernetPacket{

    private Integer label;
    private Short trafficClass;
    private boolean bottomOfStack = true;

    public Integer getLabel() {
        return label;
    }

    public void setLabel(Integer label) {
        this.label = label;
    }

    public Short getTrafficClass() {
        return trafficClass;
    }

    public void setTrafficClass(Short trafficClass) {
        this.trafficClass = trafficClass;
    }

    public boolean isBottomOfStack() {
        return bottomOfStack;
    }

    public void setBottomOfStack(boolean bottomOfStack) {
        this.bottomOfStack = bottomOfStack;
    }
}
