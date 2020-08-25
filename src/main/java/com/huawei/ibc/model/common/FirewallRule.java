package com.huawei.ibc.model.common;

import org.apache.commons.net.util.SubnetUtils;

public class FirewallRule implements Comparable<FirewallRule>{

    private int priority = 0;
    private AccessType accessType;
    private SubnetUtils sourceIp;
    private SubnetUtils destinationIp;
    private Short sourcePort;
    private Short destinationPort;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public SubnetUtils getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(SubnetUtils sourceIp) {
        this.sourceIp = sourceIp;
    }

    public SubnetUtils getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(SubnetUtils destinationIp) {
        this.destinationIp = destinationIp;
    }

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


    @Override
    public int compareTo(FirewallRule other) {
        return Integer.compare(this.priority, other.priority);
    }
}
