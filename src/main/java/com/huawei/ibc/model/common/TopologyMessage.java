package com.huawei.ibc.model.common;

import com.huawei.ibc.model.db.node.AbstractDevice;
import com.huawei.ibc.model.db.node.AbstractNode;

import java.util.HashSet;
import java.util.Set;

public class TopologyMessage {

    private Set<AbstractNode> nodes = new HashSet<>();
    private Set<ConnectionMessage>connectionSet = new HashSet<>();

    public Set<AbstractNode> getNodes() {
        return nodes;
    }

    public void addDevice(AbstractNode node){
        nodes.add(node);
    }

    public Set<ConnectionMessage> getConnectionSet() {
        return connectionSet;
    }

    public void setConnectionSet(Set<ConnectionMessage> connectionSet) {
        this.connectionSet = connectionSet;
    }

    public void addConnection(String source, String destination) {
        connectionSet.add(new ConnectionMessage(source, destination));
    }
}
