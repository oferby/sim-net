package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.client.GraphEntity;
import com.huawei.ibc.model.common.GroupType;
import com.huawei.ibc.model.common.NodeType;

import java.util.HashSet;
import java.util.Set;

public class Group extends AbstractNode{

    private GroupType groupType;
    private Set<AbstractNode> nodeSet = new HashSet<>();

    public Group(String id, GroupType groupType) {
        super(id, NodeType.GROUP);
        this.groupType = groupType;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(GroupType groupType) {
        this.groupType = groupType;
    }

    public Set<AbstractNode> getNodeSet() {
        return nodeSet;
    }

    public void setNodeSet(Set<AbstractNode> nodeSet) {
        this.nodeSet = nodeSet;
    }

    public void addNode(AbstractNode node){
        nodeSet.add(node);
    }
}
