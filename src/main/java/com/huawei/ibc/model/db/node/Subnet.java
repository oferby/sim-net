package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.NodeType;
import org.apache.commons.net.util.SubnetUtils;

import java.util.HashSet;
import java.util.Set;

public class Subnet extends AbstractNode{

    private SubnetUtils utils;
    private Set<EthernetPort> attachedPorts = new HashSet<>();

    public Subnet(String id, String cidr) {
        super(id, NodeType.SUBNET);
        utils = new SubnetUtils(cidr);
    }

    public void setSubnet(String cidr) {
         utils = new SubnetUtils(cidr);
    }

    public void attachToSubnet(EthernetPort port) {
        attachedPorts.add(port);
    }

    public String getCIDR() {
        return utils.getInfo().getCidrSignature();
    }

    public SubnetUtils getUtils() {
        return utils;
    }

    public void setUtils(SubnetUtils utils) {
        this.utils = utils;
    }
}
