package com.huawei.ibc.web.model;

import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.db.node.AbstractDevice;

public class Device {

    private final String id;
    private final NodeType nodeType;

    public String getId() {
        return id;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public Device(String id, NodeType nodeType) {
        this.id = id;
        this.nodeType = nodeType;
    }

    public static Device getInstance(AbstractDevice abstractDevice){
        return new Device(abstractDevice.getId(), abstractDevice.getNodeType());

    }


}
