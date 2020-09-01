package com.huawei.ibc.model.common;

import com.huawei.ibc.model.db.node.AbstractDevice;
import com.huawei.ibc.model.db.node.VirtualMachine;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShortestPathDescriptor {
    private VirtualMachine start;
    private VirtualMachine end;
    private Set<AbstractDevice> unvisited = new HashSet<>();
    private Set<AbstractDevice> visited = new HashSet<>();
    private Map<AbstractDevice, Map<AbstractDevice, Integer>> costMap = new HashMap<>();

    public VirtualMachine getStart() {
        return start;
    }

    public void setStart(VirtualMachine start) {
        this.start = start;
    }

    public VirtualMachine getEnd() {
        return end;
    }

    public void setEnd(VirtualMachine end) {
        this.end = end;
    }

    public boolean isVisited(AbstractDevice device) {
        return visited.contains(device);
    }

    public void setVisited(AbstractDevice device) {
        unvisited.remove(device);
        visited.add(device);
    }

    public void updateCost(AbstractDevice from, AbstractDevice to, int cost) {

        if (!visited.contains(from)){
            this.setVisited(from);
        }

        if (!costMap.containsKey(from)) {
            Map<AbstractDevice, Integer> innerMap = new HashMap<>();
            innerMap.put(to,cost);
            costMap.put(from, innerMap);
        } else {

            Map<AbstractDevice, Integer> innerMap = costMap.get(from);

            if (!innerMap.containsKey(to)){
                innerMap.put(to,cost);
            } else {

                Integer costValue = innerMap.get(to);
                innerMap.put(to, costValue + cost);
            }

        }
    }

    public int getCostForPath(AbstractDevice from, AbstractDevice to) {

        try {
            return  costMap.get(from).get(to);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }
}
