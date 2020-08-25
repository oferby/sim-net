package com.huawei.ibc.model.controller;

import com.huawei.ibc.model.db.node.Subnet;
import com.huawei.ibc.model.db.protocol.MACAddress;
import org.springframework.stereotype.Controller;

import java.util.LinkedList;
import java.util.Queue;

@Controller
public class AddressControllerImpl {

    private int lastAddress = 100;

    private Queue<String> unusedCIDR = new LinkedList<>();
    private Queue<String> usedCIDR = new LinkedList<>();

    public AddressControllerImpl() {
        init();
    }

    public MACAddress getMacAddress(){

        return MACAddress.valueOf(lastAddress++);
    }

    public void clearAll() {
        init();
    }

    public Subnet getNewSubnet() {

        String cidr = unusedCIDR.poll();
        usedCIDR.add(cidr);
        return new Subnet(cidr, cidr);

    }

    public void releaseSubnet(String cidr) {
        throw new RuntimeException("not supported.");
    }

    private void init() {

        unusedCIDR.clear();
        usedCIDR.clear();

        for (int i = 1; i < 254; i++) {
            unusedCIDR.add("192.168." + i + ".0/24");
        }
    }

}
