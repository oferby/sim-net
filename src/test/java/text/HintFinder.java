package text;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.ibc.message.IntentMessage;
import com.huawei.ibc.model.common.AccessType;
import com.huawei.ibc.model.common.FirewallRule;
import com.huawei.ibc.model.db.node.Firewall;
import com.huawei.ibc.model.db.node.PromiscuousPort;
import com.huawei.ibc.model.db.protocol.IpPacket;
import com.huawei.ibc.model.db.protocol.TcpPacket;
import com.huawei.ibc.text.antlr.AntlrHintController;
import org.apache.commons.net.util.SubnetUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//@RunWith(SpringRunner.class)
public class HintFinder {

    @Test
    public void testRegex() {

        String s = "add  vm kj sd";

        Pattern p = Pattern.compile("add\\s+vm\\s+(kj\\s+)?sd");
        Matcher m = p.matcher(s);
        boolean b = m.matches();

        assert b;


    }

    @Test
    public void testRegexGroup() {

        String s = "create  vm kj lj lsdf sd";

        Pattern p = Pattern.compile("(add|create)\\s+vm.+");
        Matcher m = p.matcher(s);
        boolean b = m.matches();

        assert b;


    }

    @Test
    public void extract() {

        Map<String, String> params = new HashMap<>();
//        String s = "set policy p1 allow traffic  from  vm1 to  db1";
//        String s = "policy p1 allow traffic from  web1 to  web2";
//        String s = "set policy p1 allow traffic to node5 from node4 ";
//        String s = "set policy p1 allow traffic from node4 to node5 ";
        String s = "show policy ";
        Pattern p = Pattern.compile("(set\\s+)?policy\\s+([a-z0-9]+)\\s+(allow|deny).*(from|to)\\s+([a-z0-9]+).*(from|to)\\s+([a-z0-9]+)\\s*");
//        Pattern p = Pattern.compile("set\\s+policy\\s+([a-z0-9]+)\\s+(allow|deny).*?(from|to)\\s+([a-z0-9]+).*?(from|to)\\s+([a-z0-9]+)");
        Matcher m = p.matcher(s);

        if (!m.find()) {
            System.out.println("error");
            return;
        }

        int groupCount = m.groupCount();

        if (groupCount < 6)
            return;

        String policyName = m.group(1);
        params.put("name", policyName);

        String grant1 = m.group(2);
        params.put("operation", grant1);

        String toFrom = m.group(3);
        String node1 = m.group(4);
        params.put(toFrom, node1);


        String toFrom2 = m.group(5);
        String node2 = m.group(6);
        params.put(toFrom2, node2);

        System.out.println(params);

    }

    @Test
    public void showPolicy() {

        List<String> commandList = new LinkedList<>();
        commandList.add("show policy p1");
        commandList.add("find policy p1 ");
        commandList.add("show policy");
        commandList.add("show policy ");
        commandList.add("show all   policies");
        commandList.add("show  all   policy");

        Pattern p = Pattern.compile("(find|show)\\s+(all\\s+)?(policy|policies)\\s*([a-z0-9]+)?\\s*");

        for (String command : commandList) {


            Matcher m = p.matcher(command);
            boolean found = m.find();
            assert found;

            if (command.trim().endsWith("p1"))
                assert m.group(4).equals("p1");
            else
                assert m.group(4) == null;
        }


    }

    @Test
    public void addVm() {

        List<String> commandList = new LinkedList<>();
        commandList.add("add vm v1  ");
        commandList.add("create    vm v1 default ");
        commandList.add("new vm v1 default");
        commandList.add("add vm  v1 default");

        Pattern p = Pattern.compile("(add|create|new|start)\\s+vm\\s+([a-z0-9]+)\\s*(default)?\\s*");

        for (String command : commandList) {

            Matcher m = p.matcher(command);
            boolean found = m.find();
            assert found;

            assert m.group(2).equals("v1");

            assert !command.trim().endsWith("default") || m.group(3).equals("default");
        }


    }


    @Test
    public void extractWord() {

        String s = "add application app1 listen on port 3306 host host1";

        Pattern p = Pattern.compile("(add|create)\\s+application\\s+([a-z0-9]+).*port\\s+([0-9]+)");
        Matcher m = p.matcher(s);

        boolean b = m.find();

        int groupCount = m.groupCount();

        String appName = m.group(2);

        String portNumber = m.group(3);

        assert portNumber != null;

        p = Pattern.compile(".*(host|vm)\\s+([a-z0-9]+).*");
        m = p.matcher(s);
        boolean found = m.find();

        String hostName = m.group(2);
        assert found;

    }

    @Test
    public void testSubnet() {

        SubnetUtils subnetUtils = new SubnetUtils("192.168.1.14/24");

        String address = subnetUtils.getInfo().getAddress();

        boolean inRange = subnetUtils.getInfo().isInRange("192.168.1.13");

        String lowAddress = subnetUtils.getInfo().getLowAddress();

        String netmask = subnetUtils.getInfo().getNetmask();

        String[] allAddresses = subnetUtils.getInfo().getAllAddresses();

        SubnetUtils subnet1 = new SubnetUtils(lowAddress, netmask);

        long addressCount = subnetUtils.getInfo().getAddressCountLong();


        assert inRange;

    }

    @Test
    public void extractPath() {

        List<String> commandList = new LinkedList<>();
        commandList.add("find path from node1 to node2");
        commandList.add("find traffic from node1 to node2");
        commandList.add("show path to node2 from node1 ");

        Map<String, String> params;
//        Pattern p = Pattern.compile("set\\s+policy\\s+([a-z0-9]+)\\s+(allow|deny).*?(from|to)\\s+([a-z0-9]+).*?(from|to)\\s+([a-z0-9]+)");
        Pattern p = Pattern.compile("(find|show)\\s+(path|traffic)\\s+(from|to)\\s+([a-z0-9]+)\\s+(from|to)\\s+([a-z0-9]+)\\s*");

        for (String command : commandList) {

            params = new HashMap<>();

            Matcher m = p.matcher(command);
            boolean found = m.find();
            assert found;

            params.put(m.group(3), m.group(4));
            params.put(m.group(5), m.group(6));

            assert params.get("from").equals("node1");
            assert params.get("to").equals("node2");
        }

    }

    @Test
    public void testFirewallRule() {

        List<String> commandList = new LinkedList<>();
        commandList.add("allow traffic from web1 to db1");
        commandList.add("allow all traffic from web1 to db1");
        commandList.add("deny all traffic to db1");
        commandList.add("deny traffic from web1 to db1");
        commandList.add("deny traffic from web1");
        commandList.add("allow all traffic to db1");


        for (String command : commandList) {

            Map<String, String> params;
            Pattern p = Pattern.compile("(allow|deny)\\s+(all\\s+)?traffic\\s+(from|to)\\s+([a-z0-9]+)\\s+(from|to)\\s+([a-z0-9]+)\\s*");

            params = new HashMap<>();

            Matcher m = p.matcher(command);
            boolean found = m.find();

            if (found) {

                params.put("access", m.group(1));

                if (m.group(2) != null)
                    params.put("all", "true");

                params.put(m.group(3), m.group(4));
                params.put(m.group(5), m.group(6));

                continue;

            }

            p = Pattern.compile("(allow|deny)\\s+(all\\s+)?traffic\\s+(from|to)\\s+([a-z0-9]+)\\s*");

            m = p.matcher(command);
            found = m.find();

            if (found) {

                params.put("access", m.group(1));

                if (m.group(2) != null)
                    params.put("all", "true");

                params.put(m.group(3), m.group(4));

                continue;

            }

        }

//        assert params.get("access").equals("allow");
//        assert params.get("from").equals("node1");
//        assert params.get("to").equals("node2");

    }

    @Test
    public void testFirewall() {


        SubnetUtils subnetUtils = new SubnetUtils("192.168.1.14/24");

        SubnetUtils in = new SubnetUtils("192.168.1.16/32");
        in.setInclusiveHostCount(true);

        boolean inRange = subnetUtils.getInfo().isInRange(in.getInfo().getAddress());

        assert inRange;

        String lowAddress = in.getInfo().getLowAddress();

        String netmask = in.getInfo().getNetmask();

        String[] allAddresses = in.getInfo().getAllAddresses();

        assert in.getInfo().isInRange("192.168.1.16");

    }

    @Test
    public void testFirewallRules() {

        Firewall firewall = new Firewall("fw1");

        for (int i = 50; i < 100; i++) {
            firewall.addRule(i, null, null, null, null, null);
        }

        for (int i = 0; i < 50; i++) {
            firewall.addRule(i, null, null, null, null, null);
        }

        FirewallRule last = null;
        for (FirewallRule rule : firewall.getFirewallRules()) {
            if (last == null) {
                last = rule;
                continue;
            }

            assert rule.getPriority() > last.getPriority();

            last = rule;
        }


    }

    @Test
    public void testFirewallRules1() {

        IpPacket ipPacket = new IpPacket();
        ipPacket.setSourceIp("192.168.1.14");
        ipPacket.setDestinationIp("192.168.1.100");

        Firewall firewall = new Firewall("fw1");
        PromiscuousPort port = new PromiscuousPort(null);

        SubnetUtils subnetUtils = new SubnetUtils("0.0.0.0/0");

        firewall.addRule(10, AccessType.ALLOW, "0.0.0.0/0", "0.0.0.0/0", null, null);

        boolean pass = true;
        try {
            firewall.rx(port, ipPacket);
            pass = false;
        } catch (NullPointerException e) {

        }

        assert pass;

        firewall.addRule(9, AccessType.DENY, "0.0.0.0/0", "0.0.0.0/0", null, null);

        pass = false;
        try {
            firewall.rx(port, ipPacket);
            pass = true;
        } catch (NullPointerException e) {

        }

        assert pass;


        firewall.addRule(8, AccessType.ALLOW, "192.168.1.14/32", "0.0.0.0/0", null, null);

        pass = true;
        try {
            firewall.rx(port, ipPacket);
            pass = false;
        } catch (NullPointerException e) {

        }

        assert pass;

        TcpPacket tcpPacket = new TcpPacket();
        tcpPacket.setSourceIp("192.168.1.14");
        tcpPacket.setDestinationIp("192.168.1.100");
        tcpPacket.setDestinationPort((short) 8080);
        tcpPacket.setSourcePort((short) 32456);

        firewall.addRule(7, AccessType.ALLOW, "0.0.0.0/0", "0.0.0.0/0", null, (short) 8080);

        pass = true;
        try {
            firewall.rx(port, ipPacket);
            pass = false;
        } catch (NullPointerException e) {

        }

        assert pass;

        firewall.addRule(6, AccessType.DENY, "192.168.1.14/32", "192.168.1.0/24", (short) 32456, null);

        pass = false;
        try {
            firewall.rx(port, ipPacket);
            pass = true;
        } catch (NullPointerException e) {

        }

        assert pass;


    }


    @Test
    public void testAntlr() {

        String s = "find path from r1 to r2";

        AntlrHintController hintController = new AntlrHintController();

        IntentMessage intentMessage = new IntentMessage();
        intentMessage.setIntent(s);

        IntentMessage hint = hintController.getHint(intentMessage);

        assert hint != null;

    }


    @Test
    public void testWitAi() throws IOException {

        String url = "https://api.wit.ai/message";
        String q = "q=add new ecs vm23";
        String v = "v=20180425";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Authorization", "Bearer RFTIDYOQQFLTUZUKECEXM2AQKE4B2AJ7");

        HttpEntity<String> entity = new HttpEntity<>(requestHeaders);

//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
//                .queryParam("q", "add new ecs vm23")
//                .queryParam("v", "20180425");
//        builder.toUriString()
//        url+"?"+q+"&"+v
        ResponseEntity<String> result = restTemplate.exchange(url + "?" + q + "&" + v, HttpMethod.GET, entity, String.class);

        System.out.println(result);


        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(result.getBody());
        JsonNode entities = root.path("entities");
        JsonNode intent = root.path("intent");

        assert entities.asText() != null;

        System.out.println(root.asText());
    }

}
