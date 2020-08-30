package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.db.protocol.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class Router extends AbstractDevice implements ForwardingDevice {

    private static final Logger logger = LoggerFactory.getLogger(Router.class);


    public Router(String id) {
        super(id, NodeType.ROUTER);
    }

    @Override
    public void rx(ForwardingPort inPort, EthernetPacket packet) {

        logger.debug("received packet: " + packet);

        if (packet instanceof DhcpRequestPacket) {
            this.handleDhcpRequest((EthernetPort)inPort, (DhcpRequestPacket)packet);
            return;
        }

        if (packet.getDestinationMac().isBroadcast())
            return;

        if (packet instanceof PathDiscoveryPacket) {
            this.handleDiscoveryPacket((PathDiscoveryPacket) packet);
            return;
        }


    }


    private synchronized void handleDhcpRequest(EthernetPort port, DhcpRequestPacket packet) {

        if (port.getAllocatedIpAddress() == null) {
            port.setAllocatedIpAddress(new HashSet<>());
            String[] allAddresses = port.getSubnetUtils().getInfo().getAllAddresses();
            allAddresses = ArrayUtils.removeElement(allAddresses,port.getIpAddress());
            port.setIpAddressAllocation(allAddresses);

        }

        for (String ipAddress : port.getIpAddressAllocation()) {
            if (!port.getAllocatedIpAddress().contains(ipAddress)) {
                this.arpTable.put(ipAddress, packet.getSourceMac());
                port.getAllocatedIpAddress().add(ipAddress);
                packet.setDestinationMac(packet.getSourceMac());
                packet.setSourceMac(port.getMacAddress());
                packet.setDestinationIp(ipAddress);
                packet.setSourceIp(port.getIpAddress());
                packet.setSubnetUtils(new SubnetUtils(ipAddress, port.getNetmask()));
                packet.setAck();
                return;
            }
        }

    }


    private void handleDiscoveryPacket(PathDiscoveryPacket packet) {

        packet.addPathNode(this);

        if (this.isForMyIp(packet))
            return;

        EthernetPort port = getOutPort(packet);
        packet.setSourceMac(port.getMacAddress());
        MACAddress macAddress = this.arpTable.get(packet.getDestinationIp());
        packet.setDestinationMac(macAddress);

        port.tx(packet);

    }


    @Override
    public void tx(EthernetPacket packet) {

    }

    @Override
    public void portUp(ForwardingPort port) {

    }

    @Override
    public void portDown(ForwardingPort port) {

    }

    private EthernetPort getOutPort(IpPacket packet) {

        String destinationIp = packet.getDestinationIp();

        for (ForwardingPort port : this.getForwardingPorts()) {
            if (((EthernetPort)port).getSubnetUtils().getInfo().isInRange(destinationIp)) {
                return (EthernetPort) port;
            }
        }

        throw new RuntimeException("could not find out port");

    }


}
