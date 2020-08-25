package com.huawei.ibc.model.db.protocol;

import com.huawei.ibc.model.db.node.AbstractDevice;
import com.huawei.ibc.model.db.node.EthernetPort;
import org.apache.commons.net.util.SubnetUtils;

public class DhcpRequestPacket extends IpPacket{

    private SubnetUtils subnetUtils;

    public DhcpRequestPacket() {
        super.setDestinationMac(MACAddress.valueOf("FF:FF:FF:FF:FF:FF"));
    }

    public DhcpRequestPacket(MACAddress source) {

        super.setSourceMac(source);

    }

    public SubnetUtils getSubnetUtils() {
        return subnetUtils;
    }

    public void setSubnetUtils(SubnetUtils subnetUtils) {
        this.subnetUtils = subnetUtils;
    }
}
