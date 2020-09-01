package com.huawei.ibc.model.common;

import com.huawei.ibc.model.db.node.AbstractDevice;

public class PathCost {

    private int cost;
    private AbstractDevice via;

    public PathCost(AbstractDevice via, int cost) {
        this.cost = cost;
        this.via = via;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public AbstractDevice getVia() {
        return via;
    }

    public void setVia(AbstractDevice via) {
        this.via = via;
    }
}
