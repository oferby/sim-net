package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.db.protocol.EthernetPacket;
import com.huawei.ibc.model.db.protocol.IpPacket;

public class Application extends AbstractNode implements ForwardingElement {

    private Short listenOnPort;
    private VirtualMachine host;

    public Application(String id) {
        super(id, NodeType.APPLICATION);
    }

    @Override
    public void rx(IpPacket packet) {



    }

    @Override
    public void tx(IpPacket packet) {
        host.rx(host.getPortList().get(0), packet);
    }

    public Short getListenOnPort() {
        return listenOnPort;
    }

    public void setListenOnPort(Short listenOnPort) {
        this.listenOnPort = listenOnPort;
    }

    public VirtualMachine getHost() {
        return host;
    }

    public void setHost(VirtualMachine host) {
        this.host = host;
    }
}
