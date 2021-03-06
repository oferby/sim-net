package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.db.protocol.EthernetPacket;
import com.huawei.ibc.model.db.protocol.IpPacket;

public interface ForwardingElement {

    void rx(EthernetPacket packet);

    void tx(EthernetPacket packet);

}
