package com.huawei.ibc.model.db.protocol;

import java.util.Objects;

public class PortLabel {
    private int port;
    private int label;

    public PortLabel(int port, int label) {
        this.port = port;
        this.label = label;
    }

    public int getPort() {
        return port;
    }

    public int getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PortLabel portLabel = (PortLabel) o;
        return port == portLabel.port &&
                label == portLabel.label;
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, label);
    }

    @Override
    public String toString() {
        return "PortLabel{" +
                "port=" + port +
                ", label=" + label +
                '}';
    }
}
