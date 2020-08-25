package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.db.protocol.EthernetPacket;

public interface ForwardingPort extends ForwardingElement{

    void setConnectedPort(ForwardingPort port);
    ForwardingPort getConnectedPort();
    AbstractDevice getPortDevice();

}
