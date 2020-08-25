package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.db.protocol.*;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class EthernetPort extends PromiscuousPort {

    private static final Logger logger = LoggerFactory.getLogger(EthernetPacket.class);

    private MACAddress macAddress;
    private SubnetUtils subnetUtils;
    protected String routerIp;
    private String[] ipAddressAllocation;
    private Set<String> allocatedIpAddress;


    public EthernetPort(MACAddress macAddress, AbstractDevice device) {
        super(device);
        this.macAddress = macAddress;
    }

    @Override
    public void rx(IpPacket packet) {

        logger.debug("got packet: " + packet);

        this.addToArpTable(packet);

        if (packet.getDestinationMac().isBroadcast() || packet.getDestinationMac().equals(this.macAddress)) {
            this.device.rx(this, packet);
            return;
        }

    }

    @Override
    public void tx(IpPacket packet) {

        packet.setSourceMac(this.macAddress);
        super.tx(packet);

    }

    public void addToArpTable(IpPacket packet){
        if (packet.getSourceIp() != null && packet.getSourceMac() != null && this.isInRange(packet.getSourceIp()))
            this.device.arpTable.put(packet.getSourceIp(), packet.getSourceMac());
    }


    public MACAddress getMacAddress() {
        return macAddress;
    }

    public void setIpAddress(String cidr) {
        subnetUtils = new SubnetUtils(cidr);
    }

    public void setIpAddress(SubnetUtils address) {
        subnetUtils = address;

    }

    public SubnetUtils getSubnetUtils() {
        return subnetUtils;
    }

    public String getIpAddress() {
        return subnetUtils.getInfo().getAddress();
    }

    public String getNetmask(){
        return subnetUtils.getInfo().getNetmask();
    }

    public void setMacAddress(MACAddress macAddress) {
        this.macAddress = macAddress;
    }

    public void setSubnetUtils(SubnetUtils subnetUtils) {
        this.subnetUtils = subnetUtils;
    }

    public String getRouterIp() {
        return routerIp;
    }

    public void setRouterIp(String routerIp) {
        this.routerIp = routerIp;
    }

    public String[] getIpAddressAllocation() {
        return ipAddressAllocation;
    }

    public void setIpAddressAllocation(String[] ipAddressAllocation) {
        this.ipAddressAllocation = ipAddressAllocation;
    }

    public Set<String> getAllocatedIpAddress() {
        return allocatedIpAddress;
    }

    public void setAllocatedIpAddress(Set<String> allocatedIpAddress) {
        this.allocatedIpAddress = allocatedIpAddress;
    }

    public boolean isInRange(String destination){
        return subnetUtils.getInfo().isInRange(destination);
    }
}
