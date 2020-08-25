package text;

import com.huawei.ibc.Application;
import com.huawei.ibc.message.IntentMessage;
import com.huawei.ibc.model.client.GraphEntity;
import com.huawei.ibc.model.common.AccessType;
import com.huawei.ibc.model.controller.AddressControllerImpl;
import com.huawei.ibc.model.controller.DatabaseControllerImpl;
import com.huawei.ibc.model.controller.GraphController;
import com.huawei.ibc.service.PolicyController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
public class PolicyTests {

    @Autowired
    private DatabaseControllerImpl databaseController;

    @Autowired
    private PolicyController policyController;

    @Autowired
    private GraphController graphController;



    @Test
    public void testPolicyValidator(){

        IntentMessage intentMessage = new IntentMessage();
        intentMessage.setIntent("buildDemo2");
        List<GraphEntity> graphEntity = graphController.getGraphEntity(intentMessage);

        boolean verifyPolicy = policyController.verifyPolicy("web1", "db1", AccessType.DENY).isOk();

        assert !verifyPolicy;

        verifyPolicy = policyController.verifyPolicy("web1", "db1", AccessType.ALLOW).isOk();

        assert verifyPolicy;

        verifyPolicy = policyController.verifyPolicy("web2", "db1", AccessType.ALLOW).isOk();

        assert !verifyPolicy;

        verifyPolicy = policyController.verifyPolicy("web2", "db1", AccessType.DENY).isOk();

        assert verifyPolicy;


    }


}
