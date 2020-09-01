package com.huawei.ibc.model.common;

import com.huawei.ibc.Application;
import com.huawei.ibc.message.IntentMessage;
import com.huawei.ibc.model.client.GraphEntity;
import com.huawei.ibc.model.controller.GraphController;
import com.huawei.ibc.model.controller.TopologyControllerImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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

    @Test
    public void testShortestPath(){

        IntentMessage intentMessage = new IntentMessage();
        intentMessage.setIntent("buildMplsDemo");

        List<GraphEntity> graphEntityList = graphController.getGraphEntity(intentMessage);

//        topologyController.findShortestPath("vm1", "vm2");

        List<MplsPathDescriptor> numberOfPossiblePaths = topologyController.findNumberOfPossiblePaths("vm1", Integer.MAX_VALUE);
        System.out.println("number of possible paths for VM1 is: " + numberOfPossiblePaths.size());

        Collections.sort(numberOfPossiblePaths);

        int minPathLength = numberOfPossiblePaths.get(0).getDeviceInPath().size();
        int maxPathLength = numberOfPossiblePaths.get(numberOfPossiblePaths.size() - 1).getDeviceInPath().size();

        System.out.println("min path length is: " + minPathLength + ", max path length is: " + maxPathLength);

        numberOfPossiblePaths = topologyController.findNumberOfPossiblePaths("vm1", 4);
        System.out.println("number of possible paths for VM1 using 4 max length is: " + numberOfPossiblePaths.size());

        numberOfPossiblePaths = topologyController.findNumberOfPossiblePaths("vm1", 3);
        System.out.println("number of possible paths for VM1 using 3 max length is: " + numberOfPossiblePaths.size());

    }






}
