package com.huawei.ibc.model.db.node;

import com.huawei.ibc.model.common.AccessType;
import com.huawei.ibc.model.common.FirewallRule;
import com.huawei.ibc.model.common.NodeType;
import com.huawei.ibc.model.db.protocol.*;
import org.apache.commons.net.util.SubnetUtils;

import java.util.Set;
import java.util.TreeSet;

public class Firewall extends AbstractDevice {

    private boolean defaultAccessType = true;

    private Set<FirewallRule> firewallRules = new TreeSet<>();

    public Firewall(String id) {
        super(id, NodeType.FIREWALL);
    }


    @Override
    public void rx(ForwardingPort inPort, IpPacket packet) {


        if (packet instanceof PathDiscoveryPacket) {
            PathDiscoveryPacket discoveryPacket = (PathDiscoveryPacket) packet;
            discoveryPacket.addPathNode(this);
        }


        if (!this.isAllowed(packet))
            return;

        getOtherPort(inPort).tx(packet);

    }

    @Override
    public void tx(IpPacket packet) {

    }

    @Override
    public void portUp(ForwardingPort port) {

    }

    private boolean isAllowed(IpPacket packet) {

        if (packet instanceof DhcpRequestPacket)
            return true;

        if (packet instanceof TcpPacket)
            return isAllowed((TcpPacket)packet);

        for (FirewallRule rule : firewallRules) {

            if (rule.getSourceIp() != null && !rule.getSourceIp().getInfo().isInRange(packet.getSourceIp()))
                continue;

            if (rule.getDestinationIp() != null && !rule.getDestinationIp().getInfo().isInRange(packet.getDestinationIp()))
                continue;

            return rule.getAccessType() == AccessType.ALLOW;

        }


        return defaultAccessType;
    }

    private boolean isAllowed(TcpPacket packet) {

        for (FirewallRule rule : firewallRules) {

            if (rule.getSourceIp() != null && !rule.getSourceIp().getInfo().isInRange(packet.getSourceIp()))
                continue;

            if (rule.getDestinationIp() != null && !rule.getDestinationIp().getInfo().isInRange(packet.getDestinationIp()))
                continue;

            if (rule.getSourcePort() != null && !rule.getSourcePort().equals(packet.getSourcePort()))
                continue;

            if (rule.getDestinationPort()!=null && !rule.getDestinationPort().equals(packet.getDestinationPort()))
                continue;

            return rule.getAccessType() == AccessType.ALLOW;

        }

        return defaultAccessType;
    }


    private ForwardingPort getOtherPort(ForwardingPort port) {

        ForwardingPort otherPort = null;
        for (ForwardingPort forwardingPort : this.getPortList()) {
            if (!forwardingPort.equals(port)) {
                otherPort = forwardingPort;
                break;
            }
        }

        return otherPort;
    }

    public void addRule(int priority, AccessType type, String fromCIDR, String toCIDR, Short fromTcpPort, Short toTcpPort) {

        FirewallRule rule = new FirewallRule();
        rule.setPriority(priority);
        rule.setAccessType(type);

        if (fromCIDR !=null) {
            SubnetUtils subnetUtils = new SubnetUtils(fromCIDR);
            subnetUtils.setInclusiveHostCount(true);
            rule.setSourceIp(subnetUtils);
        }

        if (toCIDR != null) {
            SubnetUtils subnetUtils = new SubnetUtils(toCIDR);
            subnetUtils.setInclusiveHostCount(true);
            rule.setDestinationIp(subnetUtils);
        }

        if (fromTcpPort != null) {
            rule.setSourcePort(fromTcpPort);
        }

        if (toTcpPort != null) {
            rule.setDestinationPort(toTcpPort);
        }

        this.firewallRules.add(rule);

    }

    public void clearAllRules(){
        this.firewallRules.clear();
    }

    public Set<FirewallRule> getFirewallRules() {
        return firewallRules;
    }
}
