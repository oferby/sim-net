package com.huawei.ibc.model.controller;

import com.huawei.ibc.model.common.AccessType;
import com.huawei.ibc.model.common.TopologyMessage;
import com.huawei.ibc.model.db.node.*;
import com.huawei.ibc.model.db.protocol.MACAddress;
import com.huawei.ibc.model.db.protocol.PathDiscoveryPacket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class TopologyControllerImpl {

    @Autowired
    private DatabaseControllerImpl databaseController;

    public TopologyMessage findTopology(String source, String destination) {

        TopologyMessage topologyMessage = new TopologyMessage();

        PathDiscoveryPacket packet = this.getDiscoveryPacket(source, destination);

        AbstractNode previousNode = null;
        for (AbstractNode node : packet.getPathNodes()) {
            topologyMessage.addDevice(node);
            if (previousNode != null) {
                topologyMessage.addConnection(previousNode.getId(), node.getId());
            }
            previousNode = node;
        }


        AbstractNode dNode = databaseController.getNodeById(destination);
        if (!packet.getPathNodes().peekLast().equals(dNode)) {
            topologyMessage.addDevice(dNode);
        }

        return topologyMessage;

    }


    private PathDiscoveryPacket getDiscoveryPacket(String source, String destination) {

        AbstractNode sNode = databaseController.getNodeById(source);
        AbstractNode dNode = databaseController.getNodeById(destination);

        assert sNode != null && dNode != null;

        PathDiscoveryPacket packet = new PathDiscoveryPacket();

        EthernetPort startingPort = null;
        AbstractDevice sourceDevice = null;

        if (sNode instanceof VirtualMachine || sNode instanceof Internet) {

            this.addSourceAddresses((AbstractDevice) sNode, packet);
            sourceDevice = (AbstractDevice) sNode;
            startingPort = this.addSourceAddresses(sourceDevice, packet);

        } else if (sNode instanceof Application) {

            Application app = (Application) sNode;
            sourceDevice = app.getHost();
            startingPort = this.addSourceAddresses(sourceDevice, packet);

        }

        if (dNode instanceof VirtualMachine || dNode instanceof Internet) {
            AbstractDevice device = (AbstractDevice) dNode;
            this.addDestinationAddresses(device, packet);

        } else if (dNode instanceof Application) {
            Application app = (Application) dNode;
            this.addDestinationAddresses(app.getHost(), packet);
        }

        assert packet.getDestinationIp() != null && packet.getDestinationIp() != null && startingPort != null;

        sourceDevice.tx(packet);

        return packet;

    }

    private EthernetPort addSourceAddresses(AbstractDevice device, PathDiscoveryPacket packet) {
        EthernetPort port = (EthernetPort) device.getPortList().get(0);
        MACAddress macAddress = port.getMacAddress();
        String ipAddress = null;
        try {
            ipAddress = port.getSubnetUtils().getInfo().getAddress();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        packet.setSourceMac(macAddress);
        if ( ipAddress != null)
            packet.setSourceIp(ipAddress);

        return port;
    }

    private void addDestinationAddresses(AbstractDevice device, PathDiscoveryPacket packet) {

        EthernetPort port = (EthernetPort) device.getPortList().get(0);
        MACAddress macAddress = port.getMacAddress();
        String ipAddress = null;
        try {
            ipAddress = port.getSubnetUtils().getInfo().getAddress();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        packet.setDestinationMac(macAddress);
        if ( ipAddress != null)
            packet.setDestinationIp(ipAddress);
    }

    public void addFirewallRule(AccessType type, String fromNode, String toNode) {

        if (type.equals(AccessType.ALLOW)) {

            PathDiscoveryPacket packet = this.getDiscoveryPacket(fromNode, toNode);

            VirtualMachine dNode = (VirtualMachine) databaseController.getNodeById(toNode);
            if (!packet.getPathNodes().peekLast().equals(dNode)) {

                if (packet.getPathNodes().peekLast() instanceof Firewall) {
                    Firewall firewall = (Firewall) packet.getPathNodes().peekLast();
                    int currentPriority = firewall.getFirewallRules().iterator().next().getPriority();
                    firewall.addRule(--currentPriority, type, packet.getSourceIp()+"/32", dNode.getIpAddress() + "/32",null,null);
                    return;
                }

            } else {
                return;
            }

        }

        throw new RuntimeException("not supported!");

    }

}
