package com.huawei.ibc.model.common;

import com.huawei.ibc.model.db.node.AbstractDevice;
import com.huawei.ibc.model.db.node.AbstractNode;
import com.huawei.ibc.model.db.node.VirtualMachine;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;

import java.util.*;

public class ShortestPathDescriptor {
    private VirtualMachine start;
    private VirtualMachine end;
    private Set<AbstractDevice> unvisited = new HashSet<>();
    private Set<AbstractDevice> visited = new HashSet<>();
    private Map<AbstractDevice, Map<AbstractDevice, PathCost>> costMap = new HashMap<>();

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

    public void addUnvisited(Collection<AbstractDevice> unvisited) {
        this.unvisited.addAll(unvisited);
    }

    public void updateCost(AbstractDevice from, AbstractDevice to, AbstractDevice via, int cost) {

        if (!visited.contains(from)) {
            this.setVisited(from);
        }

        if (!costMap.containsKey(from)) {
            Map<AbstractDevice, PathCost> innerMap = new HashMap<>();
            PathCost pathCost = new PathCost(via, cost);
            innerMap.put(to, pathCost);
            costMap.put(from, innerMap);

        } else {

            Map<AbstractDevice, PathCost> innerMap = costMap.get(from);

            if (innerMap.containsKey(to)) {
                PathCost pathCost = innerMap.get(to);
                pathCost.setCost(cost);
                pathCost.setVia(via);

            } else {

                PathCost pathCost = new PathCost(via, cost);
                innerMap.put(to, pathCost);
            }

        }
    }

    public int getCostForPath(AbstractDevice from, AbstractDevice to) {

        try {
            return costMap.get(from).get(to).getCost();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Integer.MAX_VALUE;
        }
    }
}
