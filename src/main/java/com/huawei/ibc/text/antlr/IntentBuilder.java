package com.huawei.ibc.text.antlr;

import com.huawei.ibc.message.IntentMessage;
import com.huawei.ibc.message.IntentStatus;
import com.huawei.ibc.model.client.GraphEntity;
import com.huawei.ibc.model.controller.GraphController;
import com.huawei.ibc.service.WebSockServiceImpl;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;
import java.util.Map;

public class IntentBuilder {

    private WebSockServiceImpl sockService;
    private GraphController graphController;

    private Map<String, String> values;
    private IntentMessage intentMessage;

    public IntentBuilder(WebSockServiceImpl sockService, GraphController graphController, IntentMessage intentMessage) {
        this.sockService = sockService;
        this.graphController = graphController;
        this.intentMessage = intentMessage;

        if (intentMessage.getStatus().equals(IntentStatus.ENTERED))
            this.processHint();

        if (intentMessage.getStatus().equals(IntentStatus.INFO) && intentMessage.getIntent().equals("addVm"))
            this.handleAddVmInfo();

    }

    private void processHint() {

        CharStream stream = CharStreams.fromString(intentMessage.getHint());
        com.huawei.ibc.antlr4.ZtnLexer lexer = new com.huawei.ibc.antlr4.ZtnLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        com.huawei.ibc.antlr4.ZtnParser parser = new com.huawei.ibc.antlr4.ZtnParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new ZtnErrorListener());

        com.huawei.ibc.antlr4.ZtnParser.ZnContext znContext;
        try {
            znContext = parser.zn();
        } catch (ParseException pe) {
            pe.printStackTrace();
            this.handleInputError();
            return;
        }

        this.values = znContext.values;

        switch (this.values.get("operator")) {
            case "show":
                this.showIntent();
                break;
            case "create":
                this.createIntent();
                break;
            case "delete":
                this.deleteIntent();
                break;
            case "findPath":
                this.findPathIntent();
                break;
            case "connect":
                this.connectIntent();
                break;
            case "disconnect":
                this.disconnectIntent();
                break;
            case "allow":
                this.allowIntent();
                break;
            case "deny":
                this.denyIntent();
                break;
            case "demo":
                this.demoIntent();
                break;
            case "clear":
                sockService.sendClearLocalIntent();
                break;
            case "setPolicy":
                this.setPolicy();
                break;
            case "addToGroup":
                this.addToGroup();
                break;

            default:
                throw new RuntimeException("unknown operator");

        }

    }

    private void showIntent() {

        String name = this.values.get("name");
        if (name != null && name.equals("all")) {
            this.intentMessage.setIntent("showAll");
            this.sendIntentWithReturn();
            return;
        }

        String entity = this.values.get("entity");
        if (entity != null) {

            if (entity.equals("policy")) {
                if (name != null) {
                    this.intentMessage.addParam("name", name);
                }

                this.sendIntentWithReturn("showPolicies");
                return;
            }

            if (entity.equals("group")) {
                if (name != null) {
                    this.intentMessage.addParam("name", name);
                }

                this.sendIntentWithReturn("showGroup");
                return;

            }

        }

        throw new RuntimeException("not supported");

    }

    private void createIntent() {

        String entity = this.values.get("entity");

        switch (entity) {

            case "vm":
                this.addVm();
                break;
            case "router":
                this.sendNamedIntent("addRouter");
                break;
            case "switch":
                this.sendNamedIntent("addSwitch");
                break;
            case "firewall":
                this.sendNamedIntent("addFirewall");
                break;
            case "service":
                this.sendNamedIntent("addService");
                break;
            case "policy":
                this.sendNamedIntent("addPolicy");
                break;
            case "group":
                this.sendNamedIntent("addGroup");
                break;

            case "application":
                this.addApplication();
                break;

            default:
                throw new RuntimeException("not supported");

        }


    }


    private void addVm() {

        if (this.values.containsKey("default")) {
            this.sendNamedIntent("addVm");
            return;
        }

        intentMessage.setStatus(IntentStatus.INFO);
        intentMessage.setIntent("addVm");
        this.handleAddVmInfo();

    }

    private void handleAddVmInfo() {

        if (intentMessage.getParamValue("cpu") == null) {

            intentMessage.addParam("type", "option");
            intentMessage.addParam("question", "How many CPUs would you like?");
            intentMessage.addParam("options", "1,2,4,8,16,32");
            intentMessage.addParam("param", "cpu");

        } else if (intentMessage.getParamValue("memory") == null) {

            intentMessage.addParam("type", "option");
            intentMessage.addParam("question", "How much memory do you need?");
            intentMessage.addParam("options", "4,8,16,32");
            intentMessage.addParam("param", "memory");

        } else if (intentMessage.getParamValue("gpu") == null) {

            intentMessage.addParam("type", "yesno");
            intentMessage.addParam("question", "Do you need GPU?");
            intentMessage.addParam("param", "gpu");

        }

        this.sendNamedIntent("addVm");

    }

    private void deleteIntent() {

        this.sendSimpleIntent("deleteAll");

    }

    private void findPathIntent() {
        intentMessage.addParam("from", this.values.get("from"));
        intentMessage.addParam("to", this.values.get("to"));

        this.sendIntentWithReturn("findPath");

    }

    private void connectIntent() {

        intentMessage.addParam("source", this.values.get("from"));
        intentMessage.addParam("target", this.values.get("to"));
        intentMessage.setIntent("connectNodes");
        List<GraphEntity> graphEntity = graphController.getGraphEntity(intentMessage);
        sockService.sendGraphEntities(graphEntity);
    }

    private void disconnectIntent() {

        String to = this.values.get("to");
        if (to != null)
            intentMessage.addParam("target", to);

        intentMessage.setIntent("disconnectNodes");
        intentMessage.addParam("source", this.values.get("from"));
        this.sendIntentWithReturn();
    }

    private void allowIntent() {

        intentMessage.addParam("access", "allow");
        this.accessIntent();

    }

    private void denyIntent() {

        intentMessage.addParam("access", "deny");
        this.accessIntent();
    }


    private void accessIntent() {

        if (this.values.containsKey("all"))
            intentMessage.addParam("all", "true");

        if (this.values.containsKey("from"))
            intentMessage.addParam("from", values.get("from"));

        if (this.values.containsKey("to"))
            intentMessage.addParam("to", values.get("to"));

        this.sendSimpleIntent("addFirewallRule");

    }


    private void demoIntent() {

        String num = this.values.get("num");

        switch (num) {
            case "1":
                intentMessage.setIntent("buildDemo1");
                break;
            case "2":
                intentMessage.setIntent("buildDemo2");
                break;
            default:
                throw new RuntimeException("not supported");
        }

        intentMessage.setStatus(IntentStatus.DONE);
        sockService.sendClearLocalIntent();
        List<GraphEntity> graphEntities = graphController.getGraphEntity(intentMessage);
        sockService.sendGraphEntities(graphEntities);
    }


    private void addApplication() {

        throw new RuntimeException("not implemented");

    }

    private void setPolicy(){

        intentMessage.addParam("name", this.values.get("name"));
        intentMessage.addParam("operation", this.values.get("rights"));
        intentMessage.addParam("from", this.values.get("to"));
        intentMessage.addParam("to", this.values.get("to"));
        this.sendIntentWithReturn("setPolicy");

    }

    private void addToGroup(){

        intentMessage.addParam("node", this.values.get("name"));
        intentMessage.addParam("group", this.values.get("group"));

        this.sendIntentWithReturn("addToGroup");

    }


    private void sendNamedIntent(String intent) {
        this.intentMessage.setIntent(intent);
        String name = this.values.get("name");
        if (name != null)
            this.intentMessage.addParam("name", name);
        this.sendIntentWithReturn();
    }

    private void sendSimpleIntent(String intent) {
        intentMessage.setIntent(intent);
        intentMessage.setStatus(IntentStatus.DONE);
        graphController.getGraphEntity(intentMessage);
    }

    private void sendIntentWithReturn(String intent) {
        intentMessage.setIntent(intent);
        this.sendIntentWithReturn();
    }

    private void sendIntentWithReturn() {
        intentMessage.setStatus(IntentStatus.DONE);
        List<GraphEntity> graphEntities = graphController.getGraphEntity(intentMessage);
        sockService.sendGraphEntities(graphEntities);

    }


    private void handleInputError() {
        this.sockService.sendUnknownInput();
    }

}
