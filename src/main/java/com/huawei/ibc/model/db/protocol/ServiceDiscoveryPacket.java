package com.huawei.ibc.model.db.protocol;

import com.huawei.ibc.model.db.topology.ServiceTopology;

public class ServiceDiscoveryPacket extends TcpPacket{

    private ServiceTopology serviceTopology;

    public ServiceTopology getServiceTopology() {
        return serviceTopology;
    }

    public void setServiceTopology(ServiceTopology serviceTopology) {
        this.serviceTopology = serviceTopology;
    }
}
