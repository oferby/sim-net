package com.huawei.ibc.model.common;

import com.huawei.ibc.model.db.node.*;

import java.util.*;

public class MplsPathDescriptor implements Comparable<MplsPathDescriptor>{

    private VirtualMachine start;
    private VirtualMachine end;
    private Set<MplsSwitch> devicesInPathSet = new HashSet<>();
    private List<MplsSwitch> deviceInPath = new LinkedList<>();
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

    public Set<MplsSwitch> getDevicesInPathSet() {
        return devicesInPathSet;
    }

    public List<MplsSwitch> getDeviceInPath() {
        return deviceInPath;
    }

    public void addDevicesInPath(MplsSwitch devicesInPath) {
        this.devicesInPathSet.add(devicesInPath);
        this.deviceInPath.add(devicesInPath);
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
                Objects.equals(devicesInPathSet, that.devicesInPathSet) &&
                Objects.equals(portList, that.portList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, devicesInPathSet, portList);
    }

    public MplsPathDescriptor copy() {

        MplsPathDescriptor pathDescriptor = new MplsPathDescriptor();

        pathDescriptor.start = this.start;
        pathDescriptor.end = this.end;
        pathDescriptor.devicesInPathSet = new HashSet<>(this.devicesInPathSet);
        pathDescriptor.deviceInPath = new LinkedList<>(this.deviceInPath);
        pathDescriptor.portList = new LinkedList<>(this.portList);

        return pathDescriptor;

    }


    @Override
    public String toString() {
        return "PathDescriptor{" +
                "start=" + start +
                ", end=" + end +
                ", devicesInPath=" + devicesInPathSet +
                ", portList=" + portList +
                '}';
    }

    @Override
    public int compareTo(MplsPathDescriptor other) {

        return Integer.compare(this.deviceInPath.size(), other.deviceInPath.size());

    }
}
