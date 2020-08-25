package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.AccessType;
import com.huawei.ibc.model.common.NodeType;

public class Policy extends AbstractNode {

    private AccessType accessType;
    private AbstractNode from;
    private AbstractNode to;
    private Group fromGroup;
    private Group toGroup;

    public Policy(String id) {
        super(id, NodeType.POLICY);
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public AbstractNode getFrom() {
        return from;
    }

    public void setFrom(AbstractNode from) {
        this.from = from;
    }

    public AbstractNode getTo() {
        return to;
    }

    public void setTo(AbstractNode to) {
        this.to = to;
    }

    public Group getFromGroup() {
        return fromGroup;
    }

    public void setFromGroup(Group fromGroup) {
        this.fromGroup = fromGroup;
    }

    public Group getToGroup() {
        return toGroup;
    }

    public void setToGroup(Group toGroup) {
        this.toGroup = toGroup;
    }
}
