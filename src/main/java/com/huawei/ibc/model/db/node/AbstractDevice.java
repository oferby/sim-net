package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.db.protocol.EthernetPacket;
import com.huawei.ibc.model.db.protocol.IpPacket;
import com.huawei.ibc.model.db.protocol.MACAddress;
import org.apache.commons.net.util.SubnetUtils;

import java.util.*;

public abstract class AbstractDevice extends AbstractNode implements ForwardingDevice {

    private List<ForwardingPort> portList = new ArrayList<>();

    protected Map<String, MACAddress > arpTable = new HashMap<>();

    public AbstractDevice(String id, NodeType nodeType) {
        super(id, nodeType);
    }

    public List<ForwardingPort> getPortList() {
        return portList;
    }

    public void setPortList(List<ForwardingPort> portList) {
        this.portList = portList;
    }

    public List<String> getConnectedDevice() {

        List<String> connectedDeviceList = new ArrayList<>();
        for (ForwardingPort port : portList) {
            connectedDeviceList.add(port.getConnectedPort().getPortDevice().getId());
        }

        return connectedDeviceList;

    }

    public ForwardingPort addPort(MACAddress macAddress) {

        ForwardingPort port;
        if (this instanceof Switch || this instanceof Firewall ) {
            port = new PromiscuousPort(this);
        } else if (this instanceof MplsSwitch) {
            port = new MplsPort(this);
        } else {
            port = new EthernetPort(macAddress, this);
        }

        portList.add(port);

        return port;
    }

    public void deletePort(ForwardingPort port) {

        portList.removeIf(next -> next == port);

    }

    @Override
    public abstract void rx(ForwardingPort inPort, EthernetPacket packet);

    public List<ForwardingPort> getForwardingPorts() {
        return this.portList;
    }

    public EthernetPort getPort(int number) {
        ForwardingPort port = this.portList.get(number);
        return (EthernetPort) port;
    }

    public SubnetUtils getSubnetUtils(int number) {
        return getPort(number).getSubnetUtils();

    }

    public MACAddress getMacAddress(int number) {
        return getPort(number).getMacAddress();
    }

    protected boolean isForMyIp(IpPacket ipPacket) {
        for (ForwardingPort port : this.getForwardingPorts()) {
            if (ipPacket.getDestinationIp().equals(((EthernetPort)port).getIpAddress()))
                return true;
        }

        return false;
    }

    protected boolean isForMyMac(IpPacket packet) {

        for (ForwardingPort port : this.getForwardingPorts()) {
            if (packet.getDestinationMac().equals(((EthernetPort)port).getMacAddress()))
                return true;
        }

        return false;

    }

    public void addMacToArpTable(String ip, MACAddress macAddress) {
        this.arpTable.put(ip, macAddress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractDevice that = (AbstractDevice) o;
        return Objects.equals(portList, that.portList) &&
                Objects.equals(arpTable, that.arpTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), portList, arpTable);
    }
}
