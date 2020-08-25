package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.db.protocol.DhcpRequestPacket;
import com.huawei.ibc.model.db.protocol.IpPacket;
import com.huawei.ibc.model.db.protocol.PathDiscoveryPacket;

public class Internet extends AbstractDevice {

    public Internet(String id) {
        super(id, NodeType.INTERNET);
    }

    @Override
    public void rx(ForwardingPort inPort, IpPacket packet) {

        if (packet instanceof PathDiscoveryPacket) {
            PathDiscoveryPacket discoveryPacket = (PathDiscoveryPacket) packet;
            discoveryPacket.addPathNode(this);

            if (this.isForMyIp(discoveryPacket))
                return;

            EthernetPort outPort = this.getOutPort(discoveryPacket);
            outPort.tx(discoveryPacket);
            return;
        }

    }

    @Override
    public void tx(IpPacket packet) {

    }

    @Override
    public void portUp(ForwardingPort port) {

        DhcpRequestPacket packet = new DhcpRequestPacket();
        port.tx(packet);
        ((EthernetPort)port).setIpAddress(packet.getSubnetUtils());
        ((EthernetPort)port).routerIp = packet.getSourceIp();
        this.arpTable.put(((EthernetPort)port).routerIp, packet.getSourceMac());


    }

    private EthernetPort getOutPort(IpPacket packet) {

        String destinationIp = packet.getDestinationIp();

        for (EthernetPort port : this.getEthernetPorts()) {
            if (port.getSubnetUtils().getInfo().isInRange(destinationIp)) {
                return port;
            }
        }

        throw new RuntimeException("could not find out port");

    }

}
