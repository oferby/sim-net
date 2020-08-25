package com.huawei.ibc.model.db.topology;

import com.huawei.ibc.model.db.node.ForwardingDevice;
import com.huawei.ibc.model.db.node.ForwardingPort;

public class ServiceChain {

    private ForwardingDevice sourcedevice;
    private ForwardingDevice destinationDevice;
    private ForwardingPort sourcePort;
    private ForwardingPort destinationPort;
    private ServiceChain nextServiceChain;

    public ForwardingDevice getSourcedevice() {
        return sourcedevice;
    }

    public void setSourcedevice(ForwardingDevice sourcedevice) {
        this.sourcedevice = sourcedevice;
    }

    public ForwardingDevice getDestinationDevice() {
        return destinationDevice;
    }

    public void setDestinationDevice(ForwardingDevice destinationDevice) {
        this.destinationDevice = destinationDevice;
    }

    public ForwardingPort getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(ForwardingPort sourcePort) {
        this.sourcePort = sourcePort;
    }

    public ForwardingPort getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(ForwardingPort destinationPort) {
        this.destinationPort = destinationPort;
    }

    public ServiceChain getNextServiceChain() {
        return nextServiceChain;
    }

    public void setNextServiceChain(ServiceChain nextServiceChain) {
        this.nextServiceChain = nextServiceChain;
    }
}
