package com.huawei.ibc.model.common;

import com.huawei.ibc.model.db.node.*;

import java.util.*;

public class MplsPathDescriptor {

    private VirtualMachine start;
    private VirtualMachine end;
    private Set<MplsSwitch> devicesInPath = new HashSet<>();
    private List<ForwardingPort> portList = new LinkedList<>();

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

    public Set<MplsSwitch> getDevicesInPath() {
        return devicesInPath;
    }

    public void addDevicesInPath(MplsSwitch devicesInPath) {
        this.devicesInPath.add(devicesInPath);
    }

    public List<ForwardingPort> getPortList() {
        return portList;
    }

    public void addPort(ForwardingPort forwardingPort) {
        this.portList.add(forwardingPort);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MplsPathDescriptor that = (MplsPathDescriptor) o;
        return Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(devicesInPath, that.devicesInPath) &&
                Objects.equals(portList, that.portList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, devicesInPath, portList);
    }

    public MplsPathDescriptor copy() {

        MplsPathDescriptor pathDescriptor = new MplsPathDescriptor();

        pathDescriptor.start = this.start;
        pathDescriptor.end = this.end;
        pathDescriptor.devicesInPath = new HashSet<>(this.devicesInPath);
        pathDescriptor.portList = new LinkedList<>(this.portList);

        return pathDescriptor;

    }


    @Override
    public String toString() {
        return "PathDescriptor{" +
                "start=" + start +
                ", end=" + end +
                ", devicesInPath=" + devicesInPath +
                ", portList=" + portList +
                '}';
    }
}
