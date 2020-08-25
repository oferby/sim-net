package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.db.protocol.EthernetPacket;
import com.huawei.ibc.model.db.protocol.MplsPacket;

public class MplsPort extends PromiscuousPort implements ForwardingPort{

    public MplsPort(AbstractDevice device) {
        super(device);
    }

    @Override
    public void rx(EthernetPacket packet) {
        if (!(packet instanceof MplsPacket))
            return;

        this.device.rx(this, packet);

    }


}
