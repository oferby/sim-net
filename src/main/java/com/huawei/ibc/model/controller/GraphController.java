package com.huawei.ibc.model.controller;

import com.huawei.ibc.message.IntentMessage;
import com.huawei.ibc.message.IntentStatus;
import com.huawei.ibc.message.PolicyVerification;
import com.huawei.ibc.model.client.*;
import com.huawei.ibc.model.client.Group;
import com.huawei.ibc.model.common.*;
import com.huawei.ibc.model.db.node.*;
import com.huawei.ibc.service.PolicyController;
import com.huawei.ibc.service.WebSockServiceImpl;
import javafx.collections.transformation.SortedList;
import javafx.scene.media.MediaPlayer;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class GraphController {

    private int numId = 10;

    @Autowired
    WebSockServiceImpl webSockService;

    @Autowired
    private DatabaseControllerImpl databaseController;

    @Autowired
    private AddressControllerImpl addressController;

    @Autowired
    private TopologyControllerImpl topologyController;

    @Autowired
    private PolicyController policyController;

    @Autowired
    @Qualifier("threadPoolTaskExecutor")
    private TaskExecutor taskExecutor;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void setupEdgeIpAddress() {

        logger.debug("setting up IP address for all VMs");
        List<AbstractNode> computeNodes = databaseController.getAllNodesByType(NodeType.COMPUTE_NODE);

        SubnetUtils subnet = new SubnetUtils("192.168.0.1", "255.255.0.0");
        String[] allAddresses = subnet.getInfo().getAllAddresses();
        int i = 0;
        for (AbstractNode computeNode : computeNodes) {
            List<ForwardingPort> ethernetPorts = ((AbstractDevice) computeNode).getForwardingPorts();
            for (ForwardingPort ethernetPort : ethernetPorts) {
                ((EthernetPort) ethernetPort).setIpAddress(allAddresses[i++] + "/16");
            }

        }

        this.sendInfo("IP address configured for all VMs");

    }

    public void setupMplsPathForAllEdges(String algorithm) {

        if (algorithm.equals("1"))
            taskExecutor.execute(() -> topologyController.setupMplsShortestPathDepthFirst());
        else if (algorithm.equals("2"))
            taskExecutor.execute(() -> topologyController.setupMplsShortestPathDijkstra());


    }

    public void calculateShortestPath(IntentMessage intentMessage) {

        String from = intentMessage.getParamValue("from");

        String to = null;
        if (intentMessage.hasParam("to"))
            to = intentMessage.getParamValue("to");

        int limit = Integer.MAX_VALUE;
        if (intentMessage.hasParam("maxLength"))
            limit = Integer.parseInt(intentMessage.getParamValue("maxLength"));

        List<MplsPathDescriptor> numberOfPossiblePaths = topologyController.findNumberOfPossiblePaths(from, to, limit);
        Collections.sort(numberOfPossiblePaths);

        int shortestPath = numberOfPossiblePaths.get(0).getDeviceInPath().size();
        int size = numberOfPossiblePaths.size();
        int last =  size - 1;
        int longestPath = numberOfPossiblePaths.get(last).getDeviceInPath().size();

        String s = "There are " + size + " paths from " + from;

        if (to != null)
            s = s + " to " + to;

        this.sendInfo( s + ". Shortest path is " + shortestPath + ". Longest path is " + longestPath);
    }

    public List<GraphEntity> getGraphEntity(IntentMessage intentMessage) {

        logger.debug("handling intent: " + intentMessage.toString());

        String intent = intentMessage.getIntent();
        List<GraphEntity> graphEntityList = new ArrayList<>();

        try {


            switch (intent) {
                case "buildDemo1":
                    return buildDemo(intentMessage);
                case "buildDemo2":
                    return this.buildDemo2(intentMessage);
                case "buildSmallMplsDemo":
                    return this.buildSmallMplsDemo();
                case "buildMplsDemo":
                    return this.buildMplsDemo(intentMessage);
                case "addVm":
                    graphEntityList.add(addVm(intentMessage));
                    return graphEntityList;
                case "addRouter":
                    graphEntityList.add(addRouter(intentMessage));
                    return graphEntityList;
                case "addSwitch":
                    graphEntityList.add(addSwitch(intentMessage));
                    return graphEntityList;
                case "addMplsSwitch":
                    graphEntityList.add(addMplsSwitch(intentMessage));
                    return graphEntityList;
                case "connectNodes":
                    graphEntityList.addAll(createNodeConnection(intentMessage));
                    return graphEntityList;
                case "disconnectNodes":
                    this.disconnectNodes(intentMessage);
                    return null;
                case "delete":
                    this.delete(intentMessage);
                    return null;
                case "showAll":
                    return showAll();
                case "addFirewall":
                    graphEntityList.add(addFirewall(intentMessage));
                    return graphEntityList;
                case "addPolicy":
                    graphEntityList.add(this.addPolicy(intentMessage));
                    return graphEntityList;
                case "setPolicy":
                    return this.setPolicy(intentMessage);
                case "showPolicies":
                    graphEntityList.addAll(this.showAllPolicies(intentMessage));
                    return graphEntityList;
                case "addApplication":
                    graphEntityList.addAll(this.createApplication(intentMessage));
                    return graphEntityList;
                case "findPath":
                    return this.findPath(intentMessage);
                case "addFirewallRule":
                    this.addFirewallRule(intentMessage);
                    return null;
                case "addGroup":
                    graphEntityList.add(this.addGroup(intentMessage));
                    return graphEntityList;
                case "showGroup":
                    graphEntityList.addAll(this.showGroup(intentMessage));
                    return graphEntityList;
                case "addToGroup":
                    return this.addToGroup(intentMessage);

            }
        } catch (RuntimeException e) {
            this.sendError(e.getMessage());
            throw e;
        }

        this.sendError("not supported!");
        throw new RuntimeException("not supported!");
    }

    private void addFirewallRule(IntentMessage intentMessage) {

        String access = intentMessage.getParamValue("access");
        AccessType accessType = AccessType.valueOf(access.toUpperCase());

        String all = intentMessage.getParamValue("all");
        String from = intentMessage.getParamValue("from");
        String to = intentMessage.getParamValue("to");

        PolicyVerification verification = policyController.verifyPolicy(from, to, accessType);
        if (!verification.isOk()) {
            this.sendError("This action violate policy: " + verification.getName());
            return;
        }

        topologyController.addFirewallRule(accessType, from, to);

    }

    private GraphEntity addPolicy(IntentMessage intentMessage) {

        Policy policy = databaseController.createPolicy(intentMessage.getParamValue("name"));
        return this.createGraphNode(policy);

    }

    private List<GraphEntity> setPolicy(IntentMessage intentMessage) {

        List<GraphEntity> graphEntityList = new ArrayList<>();

        String policyName = intentMessage.getParamValue("name");
        Policy policy = databaseController.getPolicy(policyName);
        if (policy == null)
            throw new RuntimeException("policy name not found");


        String fromNodeId = intentMessage.getParamValue("from");
        AbstractNode fromNode = databaseController.getNodeById(fromNodeId);
        if (fromNode == null) {
            fromNode = databaseController.getGroup(fromNodeId);
            if (fromNode == null)
                throw new RuntimeException("invalid from node id");
        }

        policy.setFrom(fromNode);
        graphEntityList.add(this.createGraphNode(fromNode));
        graphEntityList.add(this.createGraphEdge(fromNode.getId(), policy.getId()));

        String toNodeId = intentMessage.getParamValue("to");
        AbstractNode toNode = databaseController.getNodeById(toNodeId);
        if (toNode == null) {
            toNode = databaseController.getGroup(toNodeId);
            if (toNode == null)
                throw new RuntimeException("invalid to node id");
        }

        policy.setTo(toNode);
        graphEntityList.add(this.createGraphNode(toNode));
        graphEntityList.add(this.createGraphEdge(toNode.getId(), policy.getId()));

        switch (intentMessage.getParamValue("operation")) {

            case "allow":
                policy.setAccessType(AccessType.ALLOW);
                break;

            case "deny":
                policy.setAccessType(AccessType.DENY);
                break;

            default:
                throw new RuntimeException("invalid policy operation");

        }

        graphEntityList.add(this.createGraphNode(policy));
        webSockService.sendClearLocalIntent();
        return graphEntityList;
    }

    private Set<GraphEntity> showAllPolicies(IntentMessage intentMessage) {

        webSockService.sendClearLocalIntent();

        if (intentMessage.getParamValue("name") != null) {

            Policy policy = databaseController.getPolicy(intentMessage.getParamValue("name"));
            return this.getPolicy(policy);

        }

        Collection<Policy> policies = databaseController.getAllPolicies();

        Set<GraphEntity> graphEntities = new HashSet<>();
        for (Policy policy : policies) {
            graphEntities.addAll(this.getPolicy(policy));
        }

        return graphEntities;

    }

    private Set<GraphEntity> getPolicy(Policy policy) {

        Set<GraphEntity> graphEntities = new HashSet<>();

        GraphNode policyGraphNode = this.createGraphNode(policy);
        if (policy.getAccessType() != null)
            policyGraphNode.addToData("accessType", policy.getAccessType().toString());
        graphEntities.add(policyGraphNode);
        AbstractNode from = policy.getFrom();
        if (from != null) {
            graphEntities.add(this.createGraphNode(from));
            graphEntities.add(this.createGraphEdge(from.getId(), policy.getId()));

            if (from instanceof com.huawei.ibc.model.db.node.Group) {
                for (AbstractNode gNode : ((com.huawei.ibc.model.db.node.Group) from).getNodeSet()) {
                    graphEntities.add(this.createGraphNode(gNode));
                    graphEntities.add(this.createGraphEdge(gNode, from));
                }
            }
        }

        AbstractNode to = policy.getTo();
        if (to != null) {
            graphEntities.add(this.createGraphNode(to));
            graphEntities.add(this.createGraphEdge(to.getId(), policy.getId()));

            if (to instanceof com.huawei.ibc.model.db.node.Group) {
                for (AbstractNode toNode : ((com.huawei.ibc.model.db.node.Group) to).getNodeSet()) {
                    graphEntities.add(this.createGraphNode(toNode));
                    graphEntities.add(this.createGraphEdge(toNode, to));
                }
            }
        }

        return graphEntities;
    }

    private void disconnectNodes(IntentMessage intentMessage) {

        String sourceId = intentMessage.getParamValue("source");
        String targetId = intentMessage.getParamValue("target");
        targetId = databaseController.deleteNodeConnection(sourceId, targetId);

        intentMessage.setStatus(IntentStatus.LOCAL);
        String id = this.createGraphEdge(sourceId, targetId).getId();
        intentMessage.addParam("id", id);
        webSockService.sendIntent(intentMessage);

    }

    private void delete(IntentMessage intentMessage) {
        String name = intentMessage.getParamValue("name");

        if (name.equals("all")) {
            databaseController.deleteAll();
            addressController.clearAll();
            webSockService.sendClearLocalIntent();
            return;

        }

        databaseController.deleteNodeById(name);
        webSockService.sendDeleteNode(name);
    }

    private List<GraphEntity> showAll() {

        webSockService.sendClearLocalIntent();

        return this.getAllGraphEntities();

    }

    private List<GraphEntity> getAllGraphEntities() {
        List<GraphEntity> graphEntityList = new LinkedList<>();

        Collection<AbstractDevice> devices = databaseController.getAllDevices();
        Set<GraphEdge> graphEdgeSet = new HashSet<>();

        for (AbstractDevice device : devices) {
            graphEntityList.add(this.createGraphNode(device));

            for (ForwardingPort port : device.getPortList()) {

                graphEdgeSet.add(createGraphEdge(device.getId(),
                        port.getConnectedPort().getPortDevice().getId()));

            }

        }

        graphEntityList.addAll(graphEdgeSet);

        return graphEntityList;

    }

    private List<GraphEntity> createNodeConnection(IntentMessage intentMessage) {

        String source = intentMessage.getParamValue("source");
        String target = intentMessage.getParamValue("target");

        AbstractNode sNode = databaseController.getNodeById(source);
        AbstractNode tNode = databaseController.getNodeById(target);

        if (sNode instanceof VirtualMachine && ((VirtualMachine) sNode).getPortList().size() != 0) {
            sendError("VM " + sNode.getId() + " already connected.");
            return null;
        }

        if (tNode instanceof VirtualMachine && ((VirtualMachine) tNode).getPortList().size() != 0) {
            sendError("VM " + tNode.getId() + " already connected.");
            return null;
        }


        List<GraphEntity> entities = new LinkedList<>();
        if (sNode instanceof VirtualMachine && tNode instanceof VirtualMachine) {

            Router router = databaseController.createRouter("r" + (++numId));
            entities.add(this.createGraphNode(router));

            Switch aSwitch = databaseController.createSwitch("sw" + (++numId));
            entities.add(this.createGraphNode(aSwitch));

            databaseController.createNodeConnection(router.getId(), aSwitch.getId());
            entities.add(this.createGraphEdge(router.getId(), aSwitch.getId()));

            databaseController.createNodeConnection(sNode.getId(), aSwitch.getId());
            entities.add(this.createGraphEdge(sNode.getId(), aSwitch.getId()));
            databaseController.createNodeConnection(tNode.getId(), aSwitch.getId());
            entities.add(this.createGraphEdge(tNode.getId(), aSwitch.getId()));

        } else {

            List<AbstractDevice> devices = databaseController.createNodeConnection(source, target);
            GraphEdge graphEdge = this.createGraphEdge(devices.get(0), devices.get(1));
            entities.add(graphEdge);
        }

        return entities;
    }

    private GraphEntity addVm(IntentMessage intentMessage) {

        VirtualMachine vm = databaseController.createVirtualMachine(this.getNodeName(intentMessage));
        return this.createGraphNode(vm);

    }

    private GraphEntity addMplsSwitch(IntentMessage intentMessage) {
        MplsSwitch mplsSwitch = databaseController.createMplsSwitch(this.getNodeName(intentMessage));
        return this.createGraphNode(mplsSwitch);
    }

    private GraphEntity addRouter(IntentMessage intentMessage) {

        Router router = databaseController.createRouter(this.getNodeName(intentMessage));
        return this.createGraphNode(router);

    }

    private GraphEntity addSwitch(IntentMessage intentMessage) {

        Switch aSwitch = databaseController.createSwitch(this.getNodeName(intentMessage));
        return this.createGraphNode(aSwitch);
    }

    private GraphEntity addFirewall(IntentMessage intentMessage) {
        Firewall firewall = databaseController.createFirewall(this.getNodeName(intentMessage));
        return this.createGraphNode(firewall);
    }

    private String getNodeName(IntentMessage intentMessage) {

        String name = intentMessage.getParamValue("name");
        if (name == null) {
            name = "node" + numId++;
        }

        return name;

    }

    private List<GraphEntity> buildDemo(IntentMessage intentMessage) {

//        webSockService.sendClearLocalIntent();

        List<GraphEntity> graphEntityList = new ArrayList<>();
        List<Router> routerList = new ArrayList<>();
        for (int h = 0; h < 5; h++) {

            Router router = databaseController.createRouter("r" + h);
            graphEntityList.add(this.createGraphNode(router));
            routerList.add(router);

            for (int i = 0; i < 3; i++) {
                Switch sw = databaseController.createSwitch("sw" + (i + h * 10));
                graphEntityList.add(this.createGraphNode(sw));
                databaseController.createNodeConnection(router.getId(), sw.getId());
                graphEntityList.add(this.createGraphEdge(router.getId(), sw.getId()));

                for (int j = 0; j < 5; j++) {
                    VirtualMachine vm = databaseController.createVirtualMachine("vm" + (i * 10 + j + h * 100));
                    graphEntityList.add(this.createGraphNode(vm));
                    databaseController.createNodeConnection(sw.getId(), vm.getId());
                    graphEntityList.add(this.createGraphEdge(sw.getId(), vm.getId()));
                }
            }
        }

        Set<GraphEdge> graphEdgeSet = new HashSet<>();
        for (Router router : routerList) {
            for (Router router1 : routerList) {
                if (!router.getId().equals(router1.getId())) {
                    databaseController.createNodeConnection(router1.getId(), router.getId());
                    graphEdgeSet.add(this.createGraphEdge(router1.getId(), router.getId()));
                }
            }
        }

        graphEntityList.addAll(graphEdgeSet);


        Internet internet = (Internet) databaseController.getNodeById("Internet");
        graphEntityList.add(this.createGraphNode(internet, 1));
        Gateway gateway = databaseController.createGateway("gw1");
        graphEntityList.add(this.createGraphNode(gateway));
        databaseController.createNodeConnection(gateway.getId(), internet.getId());
        graphEntityList.add(this.createGraphEdge(gateway.getId(), internet.getId()));

        databaseController.createNodeConnection(gateway.getId(), "r1");
        databaseController.createNodeConnection(gateway.getId(), "r2");
        databaseController.createNodeConnection(gateway.getId(), "r3");
        databaseController.createNodeConnection(gateway.getId(), "r4");
        graphEntityList.add(this.createGraphEdge(gateway.getId(), "r1"));
        graphEntityList.add(this.createGraphEdge(gateway.getId(), "r2"));
        graphEntityList.add(this.createGraphEdge(gateway.getId(), "r3"));
        graphEntityList.add(this.createGraphEdge(gateway.getId(), "r4"));

        return graphEntityList;
    }

    private GraphNode createGraphNode(AbstractNode node, int weight) {

        GraphNode graphNode = this.createGraphNode(node);
        graphNode.setWeight(weight);
        return graphNode;
    }

    private GraphNode createGraphNode(AbstractNode node) {

        NodeType nodeType;
        if (node instanceof Policy) {
            AccessType accessType = ((Policy) node).getAccessType();

            if (accessType == null)
                nodeType = NodeType.POLICY;
            else if (accessType == AccessType.ALLOW)
                nodeType = NodeType.POLICY_ALLOW;
            else
                nodeType = NodeType.POLICY_DENY;
        } else
            nodeType = node.getNodeType();

        return this.createGraphNode(node.getId(), nodeType);

    }

    private GraphNode createGraphNode(String id, NodeType type) {

        GraphNode graphEntity = new GraphNode();
        graphEntity.setId(id);
        graphEntity.setType(type);
        graphEntity.setGroup(Group.NODES);

        return graphEntity;

    }

    private GraphEdge createGraphEdge(AbstractNode source, AbstractNode target) {

        return this.createGraphEdge(source.getId(), target.getId());
    }

    private GraphEdge createGraphEdge(String sourceId, String targetId) {

        GraphEdge edge = new GraphEdge();
        edge.setGroup(Group.EDGES);

        if (sourceId.compareTo(targetId) > 0) {
            String temp = sourceId;
            sourceId = targetId;
            targetId = temp;
        }

        edge.setSource(sourceId);
        edge.setTraget(targetId);

        String edgeId = sourceId + "-" + targetId;
        edge.setId(edgeId.toLowerCase());

        return edge;
    }

    private List<GraphEntity> createApplication(IntentMessage intentMessage) {

        List<GraphEntity> entities = new ArrayList<>();

        String name = intentMessage.getParamValue("name");
        String port = intentMessage.getParamValue("port");
        Application application = databaseController.createApplication(name, Short.valueOf(port));
        entities.add(this.createGraphNode(application));

        String hostName = intentMessage.getParamValue("host");
        if (hostName != null) {
            VirtualMachine vm = (VirtualMachine) databaseController.getNodeById(hostName);
            application.setHost(vm);
            entities.add(this.createGraphEdge(application.getId(), vm.getId()));
        }

        return entities;
    }

    private List<GraphEntity> buildDemo2(IntentMessage intentMessage) {

        webSockService.sendClearLocalIntent();

        List<GraphEntity> entities = new ArrayList<>();

        Router r1 = databaseController.createRouter("R1");
        entities.add(this.createGraphNode(r1));

        AbstractNode internet = databaseController.getNodeById("Internet");
        entities.add(this.createGraphNode(internet));
        databaseController.createNodeConnection(internet.getId(), r1.getId());
        entities.add(this.createGraphEdge(internet.getId(), r1.getId()));

        Switch sw1 = databaseController.createSwitch("SW1");
        entities.add(this.createGraphNode(sw1));
        databaseController.createNodeConnection(r1.getId(), sw1.getId());
        entities.add(this.createGraphEdge(r1.getId(), sw1.getId()));

        Switch sw2 = databaseController.createSwitch("SW2");
        entities.add(this.createGraphNode(sw2));
        databaseController.createNodeConnection(r1.getId(), sw2.getId());
        entities.add(this.createGraphEdge(r1.getId(), sw2.getId()));

        VirtualMachine web1 = databaseController.createVirtualMachine("WEB1");
        entities.add(this.createGraphNode(web1));
        databaseController.createNodeConnection(sw1.getId(), web1.getId());
        entities.add(this.createGraphEdge(sw1.getId(), web1.getId()));

        Application tomcat1 = databaseController.createApplication("Tomcat1", (short) 80);
        entities.add(createGraphNode(tomcat1));
        tomcat1.setHost(web1);
        entities.add(createGraphEdge(web1.getId(), tomcat1.getId()));

        VirtualMachine web2 = databaseController.createVirtualMachine("WEB2");
        entities.add(this.createGraphNode(web2));
        databaseController.createNodeConnection(sw1.getId(), web2.getId());
        entities.add(this.createGraphEdge(sw1.getId(), web2.getId()));

        Application tomcat2 = databaseController.createApplication("Tomcat2", (short) 80);
        entities.add(createGraphNode(tomcat2));
        tomcat2.setHost(web2);
        entities.add(createGraphEdge(web2.getId(), tomcat2.getId()));

        Firewall fw1 = databaseController.createFirewall("FW1");
        entities.add(this.createGraphNode(fw1));

        databaseController.createNodeConnection(sw2, fw1);
        entities.add(this.createGraphEdge(sw2.getId(), fw1.getId()));


        VirtualMachine db1 = databaseController.createVirtualMachine("DB1");
        entities.add(this.createGraphNode(db1));
        databaseController.createNodeConnection(fw1, db1);
        entities.add(this.createGraphEdge(fw1.getId(), db1.getId()));

        Application mySQL = databaseController.createApplication("MySQL", (short) 3306);
        entities.add(createGraphNode(mySQL));
        mySQL.setHost(db1);
        entities.add(createGraphEdge(mySQL.getId(), db1.getId()));

        fw1.addRule(Integer.MAX_VALUE, AccessType.DENY, "0.0.0.0/0", null, null, null);
        fw1.addRule(100, AccessType.ALLOW, web1.getIpAddress() + "/32", null, null, null);
        fw1.addRule(90, AccessType.ALLOW, db1.getIpAddress() + "/32", null, null, null);

        com.huawei.ibc.model.db.node.Group group = databaseController.createGroup("webservers", GroupType.GENERAL);
        group.addNode(web1);

        Policy policy = databaseController.createPolicy("db-access");
        policy.setTo(db1);
        policy.setFrom(group);
        policy.setAccessType(AccessType.ALLOW);

        return entities;
    }

    private List<GraphEntity> buildSmallMplsDemo() {
        webSockService.sendClearLocalIntent();

        List<GraphEntity> entities = new ArrayList<>();
        List<MplsSwitch> mplsSwitches = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            MplsSwitch mplsSwitch = databaseController.createMplsSwitch("sw" + i);
            mplsSwitches.add(mplsSwitch);
            entities.add(createGraphNode(mplsSwitch));
        }

        for (int i = 0; i < 3; i++) {
            MplsSwitch source = mplsSwitches.get(i);
            for (int j = i + 1; j < 4; j++) {
                MplsSwitch target = mplsSwitches.get(j);
                databaseController.createNodeConnection(source, target);
                entities.add(createGraphEdge(source, target));
            }
        }

        MplsSwitch start = (MplsSwitch) databaseController.getNodeById("sw4");
        MplsSwitch end = (MplsSwitch) databaseController.getNodeById("sw0");
        databaseController.createNodeConnection(start, end);
        entities.add(createGraphEdge(start, end));
        end = (MplsSwitch) databaseController.getNodeById("sw1");
        databaseController.createNodeConnection(start, end);
        entities.add(createGraphEdge(start, end));

        start = (MplsSwitch) databaseController.getNodeById("sw5");
        end = (MplsSwitch) databaseController.getNodeById("sw2");
        databaseController.createNodeConnection(start, end);
        entities.add(createGraphEdge(start, end));
        end = (MplsSwitch) databaseController.getNodeById("sw3");
        databaseController.createNodeConnection(start, end);
        entities.add(createGraphEdge(start, end));


        MplsSwitch sw = (MplsSwitch) databaseController.getNodeById("sw4");
        VirtualMachine virtualMachine = databaseController.createVirtualMachine("vm1");
        entities.add(createGraphNode(virtualMachine));
        databaseController.createNodeConnection(sw, virtualMachine);
        entities.add(createGraphEdge(sw, virtualMachine));

        sw = (MplsSwitch) databaseController.getNodeById("sw5");
        virtualMachine = databaseController.createVirtualMachine("vm2");
        entities.add(createGraphNode(virtualMachine));
        databaseController.createNodeConnection(sw, virtualMachine);
        entities.add(createGraphEdge(sw, virtualMachine));

        return entities;
    }

    private List<GraphEntity> buildMplsDemo(IntentMessage intentMessage) {
        webSockService.sendClearLocalIntent();

        List<GraphEntity> entities = new ArrayList<>();
        List<MplsSwitch> mplsSwitches = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            MplsSwitch mplsSwitch = databaseController.createMplsSwitch("SW" + i);
            mplsSwitches.add(mplsSwitch);
            entities.add(createGraphNode(mplsSwitch));
        }

        for (int i = 0; i < 3; i++) {
            MplsSwitch source = mplsSwitches.get(i);
            for (int j = i + 1; j < 4; j++) {
                MplsSwitch target = mplsSwitches.get(j);
                databaseController.createNodeConnection(source, target);
                entities.add(createGraphEdge(source, target));
            }
        }

        Random r = new Random();
        int h = 0;

        for (int i = 10; i < 20; i++) {
            MplsSwitch mplsSwitch = databaseController.createMplsSwitch("SW" + i);
            entities.add(createGraphNode(mplsSwitch));

            int rand = r.nextInt(3);

            for (int j = 0; j < 4; j++) {
                if (j == rand)
                    continue;

                MplsSwitch target = mplsSwitches.get(j);
                databaseController.createNodeConnection(mplsSwitch, target);
                entities.add(createGraphEdge(mplsSwitch, target));

                for (int k = 0; k < 10; k++) {
                    VirtualMachine virtualMachine = databaseController.createVirtualMachine("vm" + ++h);
                    entities.add(createGraphNode(virtualMachine));
                    databaseController.createNodeConnection(mplsSwitch, virtualMachine);
                    entities.add(createGraphEdge(mplsSwitch, virtualMachine));
                }

            }

        }

        return entities;
    }

    private List<GraphEntity> findPath(IntentMessage intentMessage) {

        webSockService.sendClearLocalIntent();

        List<GraphEntity> entities = new ArrayList<>();

        TopologyMessage topology = topologyController.findTopology(intentMessage.getParamValue("from"), intentMessage.getParamValue("to"));

        for (AbstractNode node : topology.getNodes()) {
            entities.add(createGraphNode(node));
        }

        for (ConnectionMessage connection : topology.getConnectionSet()) {
            entities.add(createGraphEdge(connection.getSource(), connection.getDestination()));
        }

        return entities;
    }

    private GraphEntity addGroup(IntentMessage intentMessage) {

        String name = intentMessage.getParamValue("name");
        com.huawei.ibc.model.db.node.Group group = databaseController.createGroup(name, GroupType.GENERAL);
        return this.createGraphNode(group);

    }

    private Collection<GraphEntity> showGroup(IntentMessage intentMessage) {

        Collection<GraphEntity> entities = new LinkedList<>();

        String name = intentMessage.getParamValue("name");
        if (name != null) {

            com.huawei.ibc.model.db.node.Group group = databaseController.getGroup(name);
            entities.add(this.createGraphNode(group));
            entities.addAll(this.getGroupMembers(group));


        } else {

            for (com.huawei.ibc.model.db.node.Group group : databaseController.getAllGroups()) {
                entities.add(this.createGraphNode(group));
                entities.addAll(this.getGroupMembers(group));
            }

        }

        return entities;
    }

    private Collection<GraphEntity> getGroupMembers(com.huawei.ibc.model.db.node.Group group) {

        Collection<GraphEntity> entities = new LinkedList<>();
        for (AbstractNode node : group.getNodeSet()) {
            entities.add(this.createGraphNode(node));
            entities.add(this.createGraphEdge(group.getId(), node.getId()));
        }

        return entities;

    }

    private List<GraphEntity> addToGroup(IntentMessage intentMessage) {

        com.huawei.ibc.model.db.node.Group group = databaseController.getGroup(intentMessage.getParamValue("group"));
        if (group == null)
            throw new RuntimeException("group not found.");

        AbstractNode node = databaseController.getNodeById(intentMessage.getParamValue("node"));

        group.addNode(node);

        List<GraphEntity> entities = new LinkedList<>();
        entities.add(this.createGraphNode(group));
        entities.add(this.createGraphNode(node));
        entities.add(this.createGraphEdge(group.getId(), node.getId()));

        return entities;

    }

    private void sendError(String error) {
        IntentMessage intentMessage = new IntentMessage(error, IntentStatus.ERROR, null);
        webSockService.sendIntent(intentMessage);
    }

    private void sendInfo(String text) {
        webSockService.sendInfo(text);
    }


}
