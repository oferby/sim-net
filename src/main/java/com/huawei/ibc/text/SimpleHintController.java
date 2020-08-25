package com.huawei.ibc.text;

import com.huawei.ibc.message.IntentMessage;
import com.huawei.ibc.message.IntentStatus;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@Controller
public class SimpleHintController implements HintController {

    private Set<String> commandSet;
    private Map<Pattern, String> patternMap = new HashMap<>();

    public SimpleHintController() {

        commandSet = new TreeSet<>();
        commandSet.add("build demo 1");
        commandSet.add("add demo 1");
        commandSet.add("create demo 1");
        commandSet.add("start demo 1");
        commandSet.add("add demo 2");
        commandSet.add("build demo 2");
        commandSet.add("create demo 2");
        commandSet.add("start demo 2");

        commandSet.addAll(this.hintString("vm"));
        commandSet.addAll(this.hintString("switch"));
        commandSet.addAll(this.hintString("router"));
        commandSet.addAll(this.hintString("firewall"));
        commandSet.addAll(this.hintString("service"));
        commandSet.addAll(this.hintString("policy"));
        commandSet.addAll(this.hintString("application"));

        commandSet.addAll(this.hintString("group"));
        commandSet.add("show group");

        commandSet.add("show service");
        commandSet.add("display service");
        commandSet.add("show all services");

        commandSet.add("set policy");
        commandSet.add("policy");
        commandSet.add("show policy");
        commandSet.add("show all policies");
        commandSet.add("display policy");

        commandSet.add("connect");
        commandSet.add("disconnect");

        commandSet.add("clear");
        commandSet.add("delete all");
        commandSet.add("show all");

        commandSet.add("find path from");
        commandSet.add("show path from");
        commandSet.add("find path to");
        commandSet.add("show path to");

        commandSet.add("allow traffic");
        commandSet.add("allow traffic from");
        commandSet.add("allow all traffic");
        commandSet.add("deny traffic");
        commandSet.add("deny traffic from");
        commandSet.add("deny all traffic");

        patternMap.put(Pattern.compile("(build|create|start|add)\\s+demo\\s+1\\s*"), "buildDemo1");
        patternMap.put(Pattern.compile("(build|create|start|add)\\s+demo\\s+2\\s*"), "buildDemo2");

        patternMap.put(Pattern.compile("(add|create|new|start)\\s+vm\\s+([a-zA-Z0-9]+)\\s+default\\s*"), "addVmDefault");
        patternMap.put(Pattern.compile("(add|create|new|start)\\s+vm\\s+([a-zA-Z0-9]+)\\s*"), "addVm");
        patternMap.put(createEntityPattern("switch"), "addSwitch");
        patternMap.put(createEntityPattern("router"), "addRouter");
        patternMap.put(createEntityPattern("firewall"), "addFirewall");
        patternMap.put(createEntityPattern("service"), "addService");
        patternMap.put(createEntityPattern("application"), "addApplication");
        patternMap.put(createEntityPattern("policy"), "addPolicy");

        patternMap.put(createEntityPattern("group"), "addGroup");
        patternMap.put(Pattern.compile("(show|display)\\s+group.*"), "showGroup");


        patternMap.put(Pattern.compile("(show|display)\\s+(all\\s+)?services\\s*"), "showService");
        patternMap.put(Pattern.compile("(show|display).*(policies|policy).*"), "showPolicies");
        patternMap.put(Pattern.compile("show\\s+all\\s*"), "showAll");

        patternMap.put(Pattern.compile("(set\\s+)?policy.+"), "setPolicy");

        patternMap.put(Pattern.compile("clear\\s*"), "clear");
        patternMap.put(Pattern.compile("delete\\s+all\\s*"), "deleteAll");

        patternMap.put(Pattern.compile("connect.+"), "connect");
        patternMap.put(Pattern.compile("disconnect.+"), "disconnect");

        patternMap.put(Pattern.compile("(show|find)\\s+path.+"), "findPath");

        patternMap.put(Pattern.compile("(allow|deny)\\s+(all\\s+)?traffic\\s+(from|to)\\s+([a-zA-Z0-9]+).*"), "addFirewallRule");

        patternMap.put(Pattern.compile("(add|new|create)\\s+group\\s+([a-zA-Z0-9]+)\\s*"), "addGroup");
        patternMap.put(Pattern.compile("add\\s+([a-za-zA-Z0-9]+)\\s+to\\s+group\\s+([a-zA-Z0-9]+)\\s*"), "addToGroup");

    }

    private Set<String> hintString(String entity) {

        Set<String> entityCommands = new TreeSet<>();
        entityCommands.add("add " + entity);
        entityCommands.add("create " + entity);
        entityCommands.add("new " + entity);
        entityCommands.add("start " + entity);
        return entityCommands;
    }

    private Pattern createEntityPattern(String entity) {
        return Pattern.compile("(add|create|new|start)\\s+" + entity + ".+");
    }


    public IntentMessage getHint(IntentMessage intentMessage) {

        if (intentMessage.getStatus() == IntentStatus.ENTERED) {
            return this.validateCompleteIntent(intentMessage);
        } else if (intentMessage.getStatus() == IntentStatus.HINT) {
            return this.buildHint(intentMessage);
        } else if (intentMessage.getStatus() == IntentStatus.INFO) {
            return this.handleInfo(intentMessage);
        }

        throw new RuntimeException("not supported!");
    }

    private IntentMessage buildHint(IntentMessage intentMessage) {

        String hint = intentMessage.getHint();
        int numOfWords = hint.split(" ").length;


        for (String command : commandSet) {

            if (command.startsWith(hint)) {

                String[] commandWords = command.split(" ");
                int numOfWordsInCommand = commandWords.length;
                if (numOfWords == numOfWordsInCommand) {
                    intentMessage.setHint(command);
                } else {

                    StringBuilder newHint = new StringBuilder();
                    for (int i = 0; i < numOfWords; i++) {
                        newHint.append(commandWords[i]);
                        newHint.append(" ");
                    }

                    intentMessage.setHint(newHint.toString().trim());

                }

                return intentMessage;
            }
        }

        return intentMessage;
    }


    private IntentMessage validateCompleteIntent(IntentMessage intentMessage) {

        String command = intentMessage.getHint().trim();

        String intent = null;
        for (Pattern p : patternMap.keySet()) {
            Matcher m = p.matcher(command);
            if (m.matches()) {
                intent = patternMap.get(p);
                break;
            }

        }

        if (intent == null) {
            return handleUnknown(intentMessage);
        }

        switch (intent) {
            case "buildDemo1":
                return this.doneIntent(intentMessage, "buildDemo1");
            case "buildDemo2":
                return this.doneIntent(intentMessage, "buildDemo2");
            case "clear":
                intentMessage.setStatus(IntentStatus.LOCAL);
                intentMessage.setIntent("clear");
                return intentMessage;
            case "connect":
                return this.createNodeConnectionIntent(intentMessage);
            case "disconnect":
                return this.removeNodeConnectionIntent(intentMessage);
            case "deleteAll":
                return this.getCreateNodeIntent(intentMessage, "deleteAll");
            case "addVm":
                intentMessage.setStatus(IntentStatus.INFO);
                intentMessage.setIntent("addVm");
                return this.handleAddVmInfo(intentMessage);
            case "addVmDefault":
                return this.getCreateNodeIntent(intentMessage, "addVm");
            case "addRouter":
                return this.getCreateNodeIntent(intentMessage, "addRouter");
            case "addSwitch":
                return this.getCreateNodeIntent(intentMessage, "addSwitch");
            case "addFirewall":
                return this.getCreateNodeIntent(intentMessage, "addFirewall");
            case "addService":
                return this.getCreateNodeIntent(intentMessage, "addService");
            case "addPolicy":
                return this.getCreateNodeIntent(intentMessage, "addPolicy");
            case "showAll":
                return this.doneIntent(intentMessage, "showAll");
            case "showPolicies":
                return this.showPolicy(intentMessage);
            case "showService":
                return this.doneIntent(intentMessage, "showService");
            case "setPolicy":
                return this.getSetPolicyIntent(intentMessage);
            case "addApplication":
                return this.createApplication(intentMessage);
            case "findPath":
                return this.findPath(intentMessage);
            case "addFirewallRule":
                return this.addFirewallRule(intentMessage);
            case "addGroup":
                return this.addGroup(intentMessage);
            case "showGroup":
                return this.showGroup(intentMessage);
            case "addToGroup":
                return this.addToGroup(intentMessage);
        }

        return handleUnknown(intentMessage);
    }


    private IntentMessage handleInfo(IntentMessage intentMessage) {

        String intent = intentMessage.getIntent();

        switch (intent) {

            case "addVm":
                return this.handleAddVmInfo(intentMessage);

        }

        throw new RuntimeException("not supported!");

    }


    private IntentMessage handleUnknown(IntentMessage intentMessage) {
        intentMessage.setStatus(IntentStatus.INFO);
        intentMessage.addParam("type", "unknownRequest");
        intentMessage.addParam("question", "Sorry, did not understand your request. Please rephrase.");
        return intentMessage;

    }

    private IntentMessage handleAddVmInfo(IntentMessage intentMessage) {

        if (intentMessage.getParamValue("cpu") == null) {

            intentMessage.addParam("type", "option");
            intentMessage.addParam("question", "How many CPUs would you like?");
            intentMessage.addParam("options", "1,2,4,8,16,32");
            intentMessage.addParam("param", "cpu");

            return intentMessage;

        } else if (intentMessage.getParamValue("memory") == null) {

            intentMessage.addParam("type", "option");
            intentMessage.addParam("question", "How much memory do you need?");
            intentMessage.addParam("options", "4,8,16,32");
            intentMessage.addParam("param", "memory");

            return intentMessage;

        } else if (intentMessage.getParamValue("gpu") == null) {

            intentMessage.addParam("type", "yesno");
            intentMessage.addParam("question", "Do you need GPU?");
            intentMessage.addParam("param", "gpu");

            return intentMessage;

        }

        return this.getCreateNodeIntent(intentMessage, "addVm");

    }


    private IntentMessage addFirewallRule(IntentMessage intentMessage) {

        String command = intentMessage.getHint();

        Pattern p = Pattern.compile("(allow|deny)\\s+(all\\s+)?traffic\\s+(from|to)\\s+([a-zA-Z0-9]+)\\s+(from|to)\\s+([a-zA-Z0-9]+)\\s*");

        Matcher m = p.matcher(command);
        boolean found = m.find();

        if (found) {

            intentMessage.addParam("access", m.group(1));

            if (m.group(2) != null)
                intentMessage.addParam("all", "true");

            intentMessage.addParam(m.group(3), m.group(4));
            intentMessage.addParam(m.group(5), m.group(6));

        } else {

            p = Pattern.compile("(allow|deny)\\s+(all\\s+)?traffic\\s+(from|to)\\s+([a-zA-Z0-9]+)\\s*");

            m = p.matcher(command);
            found = m.find();

            if (found) {
                intentMessage.addParam("access", m.group(1));

                if (m.group(2) != null)
                    intentMessage.addParam("all", "true");

                intentMessage.addParam(m.group(3), m.group(4));

            } else {

                throw new RuntimeException("could not parse firewall rule");
            }

        }

        this.doneIntent(intentMessage, "addFirewallRule");
        return intentMessage;
    }

    private IntentMessage removeNodeConnectionIntent(IntentMessage intentMessage) {

        String command = intentMessage.getHint();
        Pattern p = Pattern.compile("disconnect\\s+([a-zA-Z0-9]+)\\s+(from\\s+|and\\s+)?([a-zA-Z0-9]+)?\\s*");
        Matcher m = p.matcher(command);

        if (!m.find()) {
            throw new RuntimeException("could not find parameters in command");
        }


        if (m.group(3) != null)
            intentMessage.addParam("target", m.group(3));

        intentMessage.setIntent("disconnectNodes");
        intentMessage.addParam("source", m.group(1));
        intentMessage.setStatus(IntentStatus.DONE);

        return intentMessage;

    }


    private IntentMessage createNodeConnectionIntent(IntentMessage intentMessage) {

        Pattern p = Pattern.compile("connect\\s+([a-zA-Z0-9]+)\\s+(to\\s+|with\\s+|and\\s+)?([a-zA-Z0-9]+)\\s*");
        Matcher m = p.matcher(intentMessage.getHint());

        if (!m.find()) {
            throw new RuntimeException("could not find parameters in command");
        }

        intentMessage.addParam("source", m.group(1));
        intentMessage.addParam("target", m.group(3));

        intentMessage.setIntent("connectNodes");
        intentMessage.setStatus(IntentStatus.DONE);

        return intentMessage;

    }


    private IntentMessage getCreateNodeIntent(IntentMessage intentMessage, String intent) {
        String command = intentMessage.getHint();

        String nodeName = this.getNodeName(command);
        if (nodeName != null) {
            intentMessage.addParam("name", nodeName);
        }

        return doneIntent(intentMessage, intent);
    }


    private IntentMessage showPolicy(IntentMessage intentMessage) {

        Pattern p = Pattern.compile("(find|show)\\s+(all\\s+)?(policy|policies)\\s*([a-zA-Z0-9\\-_]+)?\\s*");
        Matcher m = p.matcher(intentMessage.getHint());

        m.find();
        if (m.group(4) != null)
            intentMessage.addParam("name", m.group(4));

        return doneIntent(intentMessage, "showPolicies");

    }

    private IntentMessage getSetPolicyIntent(IntentMessage intentMessage) {

        String command = intentMessage.getHint();
        Pattern p = Pattern.compile("(set\\s+)?policy\\s+([a-zA-Z0-9]+)\\s+(allow|deny).*(from|to)\\s+([a-zA-Z0-9]+).*(from|to)\\s+([a-zA-Z0-9]+)\\s*");
        Matcher m = p.matcher(command);

        if (!m.find()) {
            throw new RuntimeException("could not find parameters in command");
        }

        String policyName = m.group(2);
        intentMessage.addParam("name", policyName);

        String grant1 = m.group(3);
        intentMessage.addParam("operation", grant1);

        String toFrom = m.group(4);
        String node1 = m.group(5);
        intentMessage.addParam(toFrom, node1);


        String toFrom2 = m.group(6);
        String node2 = m.group(7);
        intentMessage.addParam(toFrom2, node2);

        intentMessage.setIntent("setPolicy");
        intentMessage.setStatus(IntentStatus.DONE);
        return intentMessage;
    }

    private IntentMessage createApplication(IntentMessage intentMessage) {

        Pattern p = Pattern.compile("(add|create)\\s+application\\s+([a-zA-Z0-9]+).*port\\s+([0-9]+)");
        Matcher m = p.matcher(intentMessage.getHint());

        boolean found = m.find();
        if (!found)
            throw new RuntimeException("invalid parameter for create application");

        intentMessage.addParam("name", m.group(2));
        intentMessage.addParam("port", m.group(3));

        p = Pattern.compile(".*(host|vm)\\s+([a-zA-Z0-9]+).*");
        m = p.matcher(intentMessage.getHint());
        found = m.find();
        if (found) {
            intentMessage.addParam("host", m.group(2));
        }

        intentMessage.setIntent("addApplication");
        intentMessage.setStatus(IntentStatus.DONE);
        return intentMessage;
    }

    private String getNodeName(String command) {
        String[] strings = command.split(" ");

        List<String> list = new ArrayList<String>(Arrays.asList(strings));
        list.removeAll(Collections.singletonList(""));
        strings = list.toArray(strings);

        if (strings.length == 2) {
            return null;
        }

        return strings[2];
    }

    private IntentMessage doneIntent(IntentMessage intentMessage, String intent) {
        intentMessage.setIntent(intent);
        intentMessage.setStatus(IntentStatus.DONE);
        return intentMessage;
    }

    private IntentMessage findPath(IntentMessage intentMessage) {

        Pattern p = Pattern.compile("(find|show)\\s+(path|traffic)\\s+(from|to)\\s+([a-zA-Z0-9]+)\\s+(from|to)\\s+([a-zA-Z0-9]+)\\s*");
        Matcher m = p.matcher(intentMessage.getHint());
        boolean found = m.find();
        assert found;

        intentMessage.addParam(m.group(3), m.group(4));
        intentMessage.addParam(m.group(5), m.group(6));
        return doneIntent(intentMessage, "findPath");
    }

    private IntentMessage addGroup(IntentMessage intentMessage) {

        String command = intentMessage.getHint();
        Pattern p = Pattern.compile("(add|new|create)\\s+group\\s+([a-zA-Z0-9]+)\\s*");
        Matcher m = p.matcher(command);

        if (!m.find()) {
            throw new RuntimeException("could not find parameters in command");
        }

        intentMessage.addParam("name", m.group(2));
        return doneIntent(intentMessage, "addGroup");

    }

    private IntentMessage showGroup(IntentMessage intentMessage) {

        String command = intentMessage.getHint();
        Pattern p = Pattern.compile("(show|display)\\s+group\\s*([a-zA-Z0-9]+\\s*)?");
        Matcher m = p.matcher(command);

        if (!m.find()) {
            throw new RuntimeException("could not find parameters in command");
        }

        if (m.group(2) != null)
            intentMessage.addParam("name", m.group(2));

        return doneIntent(intentMessage, "showGroup");


    }


    private IntentMessage addToGroup(IntentMessage intentMessage) {

        String command = intentMessage.getHint();
        Pattern p = Pattern.compile("add\\s+([a-zA-Z0-9]+)\\s+to\\s+group\\s+([a-zA-Z0-9]+)\\s*");
        Matcher m = p.matcher(command);

        if (!m.find()) {
            throw new RuntimeException("could not find parameters in command");
        }

        intentMessage.addParam("node", m.group(1));
        intentMessage.addParam("group", m.group(2));
        return doneIntent(intentMessage, "addToGroup");

    }

}
