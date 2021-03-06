package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.db.protocol.*;

public class VirtualMachine extends AbstractDevice {

    public VirtualMachine(String id) {
        super(id, NodeType.COMPUTE_NODE);
    }


    @Override
    public void rx(ForwardingPort inPort, EthernetPacket packet) {

        if (packet instanceof PathDiscoveryPacket){
            this.handleDiscoveryPacket((PathDiscoveryPacket) packet);
            return;
        }
    }

    @Override
    public void tx(EthernetPacket packet) {

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

    @Override
    public void portDown(ForwardingPort port) {

    }

    public void tx(EthernetPacket packet, EthernetPort port){

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
