package com.huawei.ibc.model.client;

public enum Group {

    NODES("nodes"), EDGES("edges");

    private final String group;

    Group(String group) {
        this.group = group;
    }

    @Override
    public String toString() {
        return group;
    }
}
