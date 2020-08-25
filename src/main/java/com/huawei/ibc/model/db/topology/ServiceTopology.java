package com.huawei.ibc.model.db.topology;

import com.huawei.ibc.model.db.node.ForwardingElement;

import java.util.List;

public class ServiceTopology {

    private List<ServiceChain> serviceChains;

    public List<ServiceChain> getServiceChains() {
        return serviceChains;
    }

    public void setServiceChains(List<ServiceChain> serviceChains) {
        this.serviceChains = serviceChains;
    }
}
