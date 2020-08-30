package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.db.protocol.EthernetPacket;
import com.huawei.ibc.model.db.protocol.PortLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MplsSwitch extends AbstractDevice implements ForwardingDevice{

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private final Map<PortLabel, Set<PortLabel>> mplsForwardTable = new HashMap<>();

    public MplsSwitch(String id) {
        super(id, NodeType.MPLS_SWITCH);
    }

    @Override
    public void rx(ForwardingPort inPort, EthernetPacket packet) {

        log.debug("received packet: " + packet.toString());

    }

    @Override
    public void tx(EthernetPacket packet) {

    }

    @Override
    public void portUp(ForwardingPort port) {

    }

    @Override
    public void portDown(ForwardingPort port) {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MplsSwitch that = (MplsSwitch) o;
        return Objects.equals(log, that.log) &&
                Objects.equals(mplsForwardTable, that.mplsForwardTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), log, mplsForwardTable);
    }
}
