package com.huawei.ibc.service;

import com.huawei.ibc.message.PolicyVerification;
import com.huawei.ibc.model.common.AccessType;
import com.huawei.ibc.model.controller.DatabaseControllerImpl;
import com.huawei.ibc.model.db.node.AbstractNode;
import com.huawei.ibc.model.db.node.Group;
import com.huawei.ibc.model.db.node.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class PolicyController {

    @Autowired
    private DatabaseControllerImpl databaseController;

    public PolicyVerification verifyPolicy(String sourceId, String targetId, AccessType requestedAccess) {

        AbstractNode fromNode = null;
        AbstractNode toNode = null;
        AccessType type = null;
        AbstractNode node = null;
        String policyName = null;
        for (Policy policy : databaseController.getAllPolicies()) {
            policyName = policy.getId();
            node = policy.getTo();

            if (node instanceof Group) {

                for (AbstractNode gNode : ((Group) node).getNodeSet()) {

                    if (gNode.getId().toLowerCase().equals(targetId.toLowerCase())) {
                        toNode = gNode;
                        break;
                    }

                }

            } else {

                if (node.getId().toLowerCase().equals(targetId.toLowerCase()))
                    toNode = node;

            }

            node = policy.getFrom();

            if (node instanceof Group) {

                for (AbstractNode gNode : ((Group) node).getNodeSet()) {

                    if (gNode.getId().toLowerCase().equals(sourceId.toLowerCase())) {
                        fromNode = gNode;
                        break;
                    }

                }

            } else {

                if (node.getId().toLowerCase().equals(sourceId.toLowerCase()))
                    fromNode = node;

            }

            if (toNode != null) {
                type = policy.getAccessType();
                break;

            }

            toNode = null;
            fromNode = null;


        }


        boolean ok = type == null ||
                (!foundBoth(fromNode, toNode, sourceId, targetId) ||
                        type.equals(requestedAccess)) && (foundBoth(fromNode, toNode, sourceId, targetId) &&
                        type.equals(requestedAccess) || (fromNode != null || !toNode.getId().toLowerCase().equals(targetId) ||
                        !type.equals(AccessType.ALLOW) || !requestedAccess.equals(AccessType.ALLOW)) &&
                        (fromNode == null && toNode.getId().toLowerCase().equals(targetId) && type.equals(AccessType.ALLOW) &&
                                requestedAccess.equals(AccessType.DENY) || fromNode == null && toNode.getId().toLowerCase().equals(targetId) &&
                                type.equals(AccessType.DENY) && requestedAccess.equals(AccessType.DENY)));


        return new PolicyVerification(policyName, ok);
    }

    private boolean foundBoth(AbstractNode fromNode, AbstractNode toNode, String sourceId, String targetId) {

        return fromNode != null && toNode != null &&
                fromNode.getId().toLowerCase().equals(sourceId) && toNode.getId().toLowerCase().equals(targetId);

    }

}
