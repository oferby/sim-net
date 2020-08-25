package com.huawei.ibc.message;

public class PolicyVerification {

    String name;
    boolean ok;

    public PolicyVerification(String name, boolean ok) {
        this.name = name;
        this.ok = ok;
    }

    public String getName() {
        return name;
    }

    public boolean isOk() {
        return ok;
    }

}
