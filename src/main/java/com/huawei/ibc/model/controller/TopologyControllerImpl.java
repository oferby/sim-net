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

    public TopologyMessage findShortestPath(String source, String destination) {

        TopologyMessage topologyMessage = new TopologyMessage();

        List<AbstractNode> mplsSwitchList = databaseController.getAllNodesByType(NodeType.MPLS_SWITCH);

        AbstractNode start = databaseController.getNodeById(source);

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

    public void setupMplsShortestPathDijkstra() {

        List<AbstractNode> allMplsSwitches = databaseController.getAllNodesByType(NodeType.MPLS_SWITCH);




    }


    public void setupMplsShortestPathDepthFirst() {

        logger.debug("searching for all paths");

//        List<MplsPathDescriptor> allPossiblePaths = new LinkedList<>();

        List<AbstractNode> allVMs = databaseController.getAllNodesByType(NodeType.COMPUTE_NODE);
        List<MplsPathDescriptor> allPathsFromVm = null;
        for (AbstractNode vm : allVMs) {
            allPathsFromVm = this.findAllPathsFromVm((VirtualMachine) vm, 3);
//            allPossiblePaths.addAll(allPathsFromVm);
            break;
        }

        assert allPathsFromVm != null;
        logger.debug("found all paths for all VMs. Number of paths found: " + allPathsFromVm.size());

        this.setupMplsPath(allPathsFromVm);

    }

    public List<MplsPathDescriptor> findNumberOfPossiblePaths(String vmId, int maxLength) {

        VirtualMachine startVm = (VirtualMachine) databaseController.getNodeById(vmId);
        return this.findAllPathsFromVm(startVm, maxLength);
    }

    private List<MplsPathDescriptor> findAllPathsFromVm(VirtualMachine start, int maxLength) {

        List<MplsPathDescriptor> mplsPathDescriptors = new LinkedList<>();

        MplsPathDescriptor pathDescriptor;

        for (ForwardingPort forwardingPort : start.getForwardingPorts()) {

            pathDescriptor = new MplsPathDescriptor();
            pathDescriptor.setStart(start);

            this.findNextDevice(forwardingPort, pathDescriptor, mplsPathDescriptors, 0, maxLength);

        }


        return mplsPathDescriptors;
    }

    private void findNextDevice(ForwardingPort egressPort, MplsPathDescriptor mplsPathDescriptor, List<MplsPathDescriptor> mplsPathDescriptors, int step, int maxLength) {

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

        if (step == maxLength)
            return;

        MplsSwitch mplsSwitch = (MplsSwitch) ingressPort.getPortDevice();

        if (mplsPathDescriptor.getDevicesInPathSet().contains(mplsSwitch))
            return;


        mplsPathDescriptor.addDevicesInPath(mplsSwitch);
        step++;

        for (ForwardingPort forwardingPort : mplsSwitch.getForwardingPorts()) {

            if (forwardingPort.equals(egressPort))
                continue;

            MplsPathDescriptor newPathDescriptor = mplsPathDescriptor.copy();
            this.findNextDevice(forwardingPort, newPathDescriptor, mplsPathDescriptors, step, maxLength);
            }

    }

    private void setupMplsPath(List<MplsPathDescriptor> allPossiblePaths) {


    }

}
