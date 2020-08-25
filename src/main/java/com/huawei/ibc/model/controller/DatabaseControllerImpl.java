package com.huawei.ibc.model.controller;

import com.huawei.ibc.model.common.GroupType;
import com.huawei.ibc.model.db.node.*;
import com.huawei.ibc.model.db.protocol.DhcpRequestPacket;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class DatabaseControllerImpl {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseControllerImpl.class);

    private Map<String, Policy> policyMap = new HashMap<>();
    private Map<String, Group> groupMap = new HashMap<>();
    private Map<String, AbstractNode> nodeMap = new HashMap<>();

    @Autowired
    private AddressControllerImpl addressController;

    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    private TaskExecutor taskExecutor;

    public DatabaseControllerImpl() {
        this.addInternalNodes();
    }

    public void deleteAll() {
        nodeMap.clear();
        groupMap.clear();
        policyMap.clear();
        this.addInternalNodes();
    }

    public AbstractNode getNodeById(String id) {
        return nodeMap.get(id.toLowerCase());
    }

    public <T extends AbstractNode> T getNodeByIdAndType(String id, Class<T> type) {
        return type.cast(nodeMap.get(id.toLowerCase()));
    }


    private void addInternalNodes() {

        String name = "Internet";
        Internet internet = new Internet(name);
        nodeMap.put(name.toLowerCase(), internet);

        name = "All";
        Group all = new Group(name, GroupType.GENERAL);
        groupMap.put(name.toLowerCase(), all);
    }

    private void validateUniqueName(String name) {
        if (nodeMap.containsKey(name))
            throw new RuntimeException("name already exists");
    }

    public VirtualMachine createVirtualMachine(String name) {

        this.validateUniqueName(name);
        VirtualMachine vm = new VirtualMachine(name);
        nodeMap.put(name.toLowerCase(), vm);

        return vm;

    }

    public Router createRouter(String name) {

        this.validateUniqueName(name);
        Router router = new Router(name);
        nodeMap.put(name.toLowerCase(), router);

        return router;
    }

    public Switch createSwitch(String name) {

        this.validateUniqueName(name);
        Switch aSwitch = new Switch(name);
        nodeMap.put(name.toLowerCase(), aSwitch);
        return aSwitch;
    }

    public Firewall createFirewall(String name) {
        this.validateUniqueName(name);
        Firewall firewall = new Firewall(name);
        nodeMap.put(name.toLowerCase(), firewall);
        return firewall;

    }

    public Gateway createGateway(String name) {
        this.validateUniqueName(name);
        Gateway gateway = new Gateway(name);
        nodeMap.put(name.toLowerCase(), gateway);
        return gateway;
    }

    public Application createApplication(String name, Short listerOnPort) {
        Application application = new Application(name);
        application.setListenOnPort(listerOnPort);
        nodeMap.put(name.toLowerCase(), application);
        return application;
    }

    public List<AbstractDevice> createNodeConnection(String sourceId, String targetId) {

        AbstractDevice sourceDevice = (AbstractDevice) nodeMap.get(sourceId.toLowerCase());

        AbstractDevice targetDevice = (AbstractDevice) nodeMap.get(targetId.toLowerCase());

        if (targetDevice == null || sourceDevice == null) {
            throw new RuntimeException("could not connect " + sourceId + " and " + targetId);
        }

        ForwardingPort sourcePort = sourceDevice.addPort(addressController.getMacAddress());
        ForwardingPort targetPort = targetDevice.addPort(addressController.getMacAddress());

        if (sourceDevice instanceof Router) {
            addSubnetToRouter((EthernetPort) sourcePort);
        } else if (targetDevice instanceof Router) {
            addSubnetToRouter((EthernetPort) targetPort);
        }

        sourcePort.setConnectedPort(targetPort);
        targetPort.setConnectedPort(sourcePort);

        List<AbstractDevice> devices = new LinkedList<>();
        devices.add(sourceDevice);
        devices.add(targetDevice);
        return devices;
    }

    private void addSubnetToRouter(EthernetPort routerPort){

        Subnet subnet = addressController.getNewSubnet();

        String lowAddress = subnet.getUtils().getInfo().getLowAddress();

        String netmask = subnet.getUtils().getInfo().getNetmask();

        SubnetUtils routerSubnet = new SubnetUtils(lowAddress, netmask);
        routerPort.setIpAddress(routerSubnet);

    }


    public void deleteNodeConnection(String sourceId, String targetId) {

        AbstractDevice sourceDevice = (AbstractDevice) nodeMap.get(sourceId.toLowerCase());
        if (sourceDevice == null)
            throw new RuntimeException("source id not found");

        AbstractDevice targetDevice = (AbstractDevice) nodeMap.get(targetId.toLowerCase());
        if (targetDevice == null) {
            throw new RuntimeException("target id not found");
        }

        for (ForwardingPort port : sourceDevice.getPortList()) {
            if (port.getConnectedPort().getPortDevice().getId().equals(targetId)) {
                sourceDevice.deletePort(port);
                targetDevice.deletePort(port.getConnectedPort());
                break;
            }
        }

    }

    public Collection<AbstractDevice> getAllDevices() {

        Collection<AbstractDevice> devices = new ArrayList<>();
        Collection<AbstractNode> nodes = nodeMap.values();
        nodes.removeIf(node -> !(node instanceof AbstractDevice));
        for (AbstractNode node : nodes) {
            devices.add((AbstractDevice) node);
        }

        return devices;
    }

    public Group createGroup(String groupId, GroupType groupType) {

        this.validateUniqueName(groupId);
        Group group = new Group(groupId, groupType);
        groupMap.put(groupId.toLowerCase(), group);
        return group;

    }

    public Group getGroup(String id) {
        return groupMap.get(id.toLowerCase());
    }

    public void deleteGroup(String id) {
        groupMap.remove(id);
    }

    public void addNodesToGroup(String groupId, Set<AbstractNode> nodeSet) {

        Group group = groupMap.get(groupId.toLowerCase());
        Set<AbstractNode> nodes = group.getNodeSet();
        if (nodes == null) {
            nodes = new HashSet<>();
        }
        nodes.addAll(nodeSet);
        group.setNodeSet(nodeSet);

    }

    public Set<AbstractNode> getGroupNodes(String groupId) {
        return groupMap.get(groupId.toLowerCase()).getNodeSet();
    }

    public Policy createPolicy(String policyName) {
        Policy policy = new Policy(policyName);
        policyMap.put(policyName.toLowerCase(), policy);
        return policy;
    }

    public Policy getPolicy(String name) {
        return policyMap.get(name.toLowerCase());
    }

    public Collection<Policy> getAllPolicies() {
        return this.policyMap.values();
    }

    public Collection<Group> getAllGroups() {
        return this.groupMap.values();
    }

    public void deleteTargetPolicy(String targetId) {
        policyMap.remove(targetId);
    }


}
