package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.db.protocol.DhcpRequestPacket;
import com.huawei.ibc.model.db.protocol.IpPacket;
import com.huawei.ibc.model.db.protocol.MACAddress;
import com.huawei.ibc.model.db.protocol.PathDiscoveryPacket;

public class VirtualMachine extends AbstractDevice {

    public VirtualMachine(String id) {
        super(id, NodeType.COMPUTE_NODE);
    }


    @Override
    public void rx(ForwardingPort inPort, IpPacket packet) {

        if (packet instanceof PathDiscoveryPacket){
            this.handleDiscoveryPacket((PathDiscoveryPacket) packet);
            return;
        }
    }

    @Override
    public void tx(IpPacket packet) {

        if (packet instanceof PathDiscoveryPacket){
            this.handleDiscoveryPacket((PathDiscoveryPacket) packet);
            return;
        }

    }

    @Override
    public void portUp(ForwardingPort port) {

        DhcpRequestPacket packet = new DhcpRequestPacket();
        port.tx(packet);
        ((EthernetPort)port).setIpAddress(packet.getSubnetUtils());
        ((EthernetPort)port).routerIp = packet.getSourceIp();
        this.arpTable.put(((EthernetPort)port).routerIp, packet.getSourceMac());

    }

    public void tx(IpPacket packet, EthernetPort port){

    }


    private void handleDiscoveryPacket(PathDiscoveryPacket packet){

        packet.addPathNode(this);

        if (this.isForMyIp(packet))
            return;

        EthernetPort port = getPort(0);

        if (port.isInRange(packet.getDestinationIp())) {
            port.tx(packet);
            return;
        } else {
            MACAddress routerMac = this.arpTable.get(port.routerIp);
            packet.setDestinationMac(routerMac);
            port.tx(packet);
            return;
        }


    }

    public String getIpAddress(){
        return getPort(0).getIpAddress();
    }


}
