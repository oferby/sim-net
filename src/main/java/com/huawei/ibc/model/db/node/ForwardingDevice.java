package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.db.protocol.IpPacket;

public interface ForwardingDevice{

    void rx(ForwardingPort inPort, IpPacket packet);

    void tx(IpPacket packet);

    void portUp(ForwardingPort port);

}
