package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;

public class AbstractNode {

    private String id;
    private NodeType nodeType;

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
}
