package com.huawei.ibc.model.controller;

import com.huawei.ibc.model.common.AccessType;
import com.huawei.ibc.model.common.MplsPathDescriptor;
import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.common.TopologyMessage;
import com.huawei.ibc.model.db.node.*;
import com.huawei.ibc.model.db.protocol.MACAddress;
import com.huawei.ibc.model.db.protocol.PathDiscoveryPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.LinkedList;
import java.util.List;

@Controller
public class TopologyControllerImpl {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        if (ipAddress != null)
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
        if (ipAddress != null)
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
                    firewall.addRule(--currentPriority, type, packet.getSourceIp() + "/32", dNode.getIpAddress() + "/32", null, null);
                    return;
                }

            } else {
                return;
            }

        }

        throw new RuntimeException("not supported!");

    }

    public void setupMplsShortestPath() {

        List<MplsPathDescriptor> allPossiblePaths = new LinkedList<>();

        List<AbstractNode> allVMs = databaseController.getAllNodesByType(NodeType.COMPUTE_NODE);

        for (AbstractNode vm : allVMs) {
            List<MplsPathDescriptor> allPathsFromVm = this.findAllPathsFromVm((VirtualMachine) vm);
            allPossiblePaths.addAll(allPathsFromVm);
        }

        logger.debug("found all paths for all VMs. Number of paths found: " + allPossiblePaths.size());

        this.setupMplsPath(allPossiblePaths);

    }

    private List<MplsPathDescriptor> findAllPathsFromVm(VirtualMachine start) {

        List<MplsPathDescriptor> mplsPathDescriptors = new LinkedList<>();

        MplsPathDescriptor pathDescriptor;

        for (ForwardingPort forwardingPort : start.getForwardingPorts()) {

            pathDescriptor = new MplsPathDescriptor();
            pathDescriptor.setStart(start);

            this.findNextDevice(forwardingPort, pathDescriptor, mplsPathDescriptors);

        }


        return mplsPathDescriptors;
    }

    private void findNextDevice(ForwardingPort egressPort, MplsPathDescriptor mplsPathDescriptor, List<MplsPathDescriptor> mplsPathDescriptors) {

        if (egressPort.getConnectedPort() == null)
            return;

        mplsPathDescriptor.addPort(egressPort);

        ForwardingPort ingressPort = egressPort.getConnectedPort();
        mplsPathDescriptor.addPort(ingressPort);

        if (ingressPort.getPortDevice() instanceof VirtualMachine) {
            mplsPathDescriptor.setEnd((VirtualMachine) ingressPort.getPortDevice());
            mplsPathDescriptors.add(mplsPathDescriptor);
            return;
        }

        MplsSwitch mplsSwitch = (MplsSwitch) ingressPort.getPortDevice();

        if (mplsPathDescriptor.getDevicesInPath().contains(mplsSwitch))
            return;

        mplsPathDescriptor.addDevicesInPath(mplsSwitch);

        for (ForwardingPort forwardingPort : mplsSwitch.getForwardingPorts()) {

            if (forwardingPort.equals(egressPort))
                continue;

            MplsPathDescriptor newPathDescriptor = mplsPathDescriptor.copy();
            this.findNextDevice(forwardingPort, newPathDescriptor, mplsPathDescriptors);
            }

    }

    private void setupMplsPath(List<MplsPathDescriptor> allPossiblePaths){

    }

}
