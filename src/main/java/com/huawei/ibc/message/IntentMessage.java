package com.huawei.ibc.message;

import java.util.HashMap;
import java.util.Map;

public class IntentMessage {

    private String hint;
    private IntentStatus status;
    private String intent;
    private Map<String,String> params;

    public IntentMessage() {
    }

    public IntentMessage(String hint, IntentStatus status, String intent) {
        this.hint = hint;
        this.status = status;
        this.intent = intent;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public IntentStatus getStatus() {
        return status;
    }

    public void setStatus(IntentStatus status) {
        this.status = status;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getParamValue(String key){
        if (params==null){
            return null;
        }

        return params.get(key);
    }


    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public void addParam(String key, String value){
        if (params==null){
            params = new HashMap<>();
        }
        params.put(key, value);
    }
}
