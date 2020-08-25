package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.db.protocol.EthernetPacket;
import com.huawei.ibc.model.db.protocol.IpPacket;
import com.huawei.ibc.model.db.protocol.MACAddress;
import org.apache.commons.net.util.SubnetUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (this instanceof Switch || this instanceof Firewall) {
            port = new PromiscuousPort(this);
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
    public abstract void rx(ForwardingPort inPort, IpPacket packet);

    public List<EthernetPort> getEthernetPorts() {
        return (List<EthernetPort>) (List<?>) this.portList;
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
        for (EthernetPort port : this.getEthernetPorts()) {
            if (ipPacket.getDestinationIp().equals(port.getIpAddress()))
                return true;
        }

        return false;
    }

    protected boolean isForMyMac(IpPacket packet) {

        for (EthernetPort port : this.getEthernetPorts()) {
            if (packet.getDestinationMac().equals(port.getMacAddress()))
                return true;
        }

        return false;

    }

    public void addMacToArpTable(String ip, MACAddress macAddress) {
        this.arpTable.put(ip, macAddress);
    }

}
