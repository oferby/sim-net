package com.huawei.ibc.model.common;

import com.huawei.ibc.Application;
import com.huawei.ibc.message.IntentMessage;
import com.huawei.ibc.model.client.GraphEntity;
import com.huawei.ibc.model.controller.GraphController;
import com.huawei.ibc.model.controller.TopologyControllerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.tags.EditorAwareTag;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
public class TestShortestPath {

    @Autowired
    private TopologyControllerImpl topologyController;

    @Autowired
    private GraphController graphController;

    @Before
    public void setup() {

        IntentMessage intentMessage = new IntentMessage();
//        intentMessage.setIntent("buildMplsDemo");
        intentMessage.setIntent("buildSmallMplsDemo");

        graphController.getGraphEntity(intentMessage);

    }

    @Test
    public void testPossiblePath(){

        List<MplsPathDescriptor> numberOfPossiblePaths = topologyController.findNumberOfPossiblePaths("vm1", Integer.MAX_VALUE);
        System.out.println("number of possible paths for VM1 is: " + numberOfPossiblePaths.size());
        System.out.println("number of paths to VM300: " + findNumberOdPathToEndVm(numberOfPossiblePaths));

        Collections.sort(numberOfPossiblePaths);

        int minPathLength = numberOfPossiblePaths.get(0).getDeviceInPath().size();
        int maxPathLength = numberOfPossiblePaths.get(numberOfPossiblePaths.size() - 1).getDeviceInPath().size();

        System.out.println("min path length is: " + minPathLength + ", max path length is: " + maxPathLength);

        numberOfPossiblePaths = topologyController.findNumberOfPossiblePaths("vm1", 4);
        System.out.println("number of possible paths for VM1 using 4 max length is: " + numberOfPossiblePaths.size());
        System.out.println("number of paths to VM300: " + findNumberOdPathToEndVm(numberOfPossiblePaths));

        numberOfPossiblePaths = topologyController.findNumberOfPossiblePaths("vm1", 3);
        System.out.println("number of possible paths for VM1 using 3 max length is: " + numberOfPossiblePaths.size());
        System.out.println("number of paths to VM300: " + findNumberOdPathToEndVm(numberOfPossiblePaths));

    }

    private int findNumberOdPathToEndVm(List<MplsPathDescriptor> numberOfPossiblePaths) {

        String endVM = "vm300";

        int i = 0;

        for (MplsPathDescriptor possiblePath : numberOfPossiblePaths) {
            if (possiblePath.getEnd().getId().equals(endVM))
                i++;
        }

        return i;
    }

    @Test
    public void testShortestPath() {

        List<MplsPathDescriptor> numberOfPossiblePaths = topologyController.findNumberOfPossiblePaths("vm1", Integer.MAX_VALUE);
        System.out.println("number of possible paths for VM1 is: " + numberOfPossiblePaths.size());

        numberOfPossiblePaths = topologyController.findNumberOfPossiblePaths("vm1", "vm2", Integer.MAX_VALUE);
        System.out.println("number of possible paths for VM1 is: " + numberOfPossiblePaths.size());

    }






}
