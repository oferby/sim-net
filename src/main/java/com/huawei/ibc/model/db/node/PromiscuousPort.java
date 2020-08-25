package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.db.node.AbstractDevice;
import com.huawei.ibc.model.db.node.EthernetPort;
import com.huawei.ibc.model.db.node.ForwardingPort;
import com.huawei.ibc.model.db.protocol.EthernetPacket;
import com.huawei.ibc.model.db.protocol.IpPacket;

public class PromiscuousPort implements ForwardingPort {

    protected ForwardingPort connectedTo;
    protected AbstractDevice device;

    public PromiscuousPort(AbstractDevice device) {
        this.device = device;
    }

    @Override
    public void rx(IpPacket packet) {

        device.rx(this, packet);

    }

    @Override
    public void tx(IpPacket packet) {

        packet.setTtl();
        connectedTo.rx(packet);

    }

    @Override
    public void setConnectedPort(ForwardingPort port) {
        this.connectedTo = port;
        if (port != null) {
            this.device.portUp(this);
        }
    }

    @Override
    public ForwardingPort getConnectedPort() {
        return this.connectedTo;
    }

    @Override
    public AbstractDevice getPortDevice() {
        return device;
    }

    public ForwardingPort getConnectedTo() {
        return connectedTo;
    }

    public void setConnectedTo(EthernetPort connectedTo) {
        this.connectedTo = connectedTo;
    }

    public AbstractDevice getDevice() {
        return device;
    }

    public void setDevice(AbstractDevice device) {
        this.device = device;
    }
}
