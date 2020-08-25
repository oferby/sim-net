package com.huawei.ibc.service;

import com.huawei.ibc.message.IntentMessage;
import com.huawei.ibc.message.IntentStatus;
import com.huawei.ibc.model.client.GraphEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WebSockServiceImpl {

    @Autowired
    private SimpMessagingTemplate template;

    public void sendIntent(String target, IntentMessage intentMessage) {

        template.convertAndSend(target, intentMessage);

    }

    public void sendIntent(IntentMessage intentMessage) {

        template.convertAndSend("/topic/hint", intentMessage);
    }

    public void sendClearLocalIntent() {
        IntentMessage intentMessage = new IntentMessage();
        intentMessage.setStatus(IntentStatus.LOCAL);
        intentMessage.setIntent("clear");
        template.convertAndSend("/topic/hint", intentMessage);
    }

    public void sendGraphEntities(List<GraphEntity> graphEntities) {
        template.convertAndSend("/topic/graph",graphEntities);
    }

    public void sendUnknownInput(){

        IntentMessage intentMessage = new IntentMessage();
        intentMessage.setStatus(IntentStatus.INFO);
        intentMessage.addParam("type", "unknownRequest");
        intentMessage.addParam("question", "Sorry, did not understand your request. Please rephrase.");
        template.convertAndSend("/topic/hint",intentMessage);
    }

}
