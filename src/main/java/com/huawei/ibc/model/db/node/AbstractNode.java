package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;

import java.util.Objects;

public class AbstractNode {

    private final String id;
    private final NodeType nodeType;

    public AbstractNode(String id, NodeType nodeType) {
        this.id = id;
        this.nodeType = nodeType;
    }

    public String getId() {
        return id;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNode node = (AbstractNode) o;
        return Objects.equals(id, node.id) &&
                nodeType == node.nodeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nodeType);
    }
}
