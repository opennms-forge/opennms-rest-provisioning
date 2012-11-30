/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package de.dertak.opennms.restprovisioning;

import com.sun.jersey.client.apache.ApacheHttpClient;
import de.dertak.opennms.restclientapi.helper.RestHelper;
import de.dertak.opennms.restclientapi.manager.RestRequisitionManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Markus@OpenNMS.org
 */
class RestCategoryProvisioner {

    private static Logger logger = LoggerFactory.getLogger(RestCategoryProvisioner.class);

    private String baseUrl;

    private String userName;

    private String password;

    private File odsFile;

    private String requisition;

    private boolean apply = false;

    private ApacheHttpClient httpClient;
    
    private RestRequisitionManager requisitionManager;

    public RestCategoryProvisioner(String baseUrl, String userName, String password, File odsFile, String requisition, Boolean apply) {
        this.baseUrl = baseUrl;
        this.userName = userName;
        this.password = password;
        this.odsFile = odsFile;
        this.requisition = requisition;
        this.apply = apply;
        this.httpClient = RestHelper.createApacheHttpClient(userName, password);
    }

    public List<RequisitionNode> getRequisitionNodesToUpdate() {
        //create and prepare RestRequisitionManager
        requisitionManager = new RestRequisitionManager(httpClient, baseUrl);
        requisitionManager.loadNodesByLableForRequisition(requisition, "");

        //read node to category mappings from spreadsheet
        SpreadsheetReader spreadsheetReader = new SpreadsheetReader();
        List<NodeToCategoryMapping> nodeToCategoryMappings = spreadsheetReader.getNodeToCategoryMappingsFromFile(odsFile, requisition);

        List<RequisitionNode> requisitionNodesToUpdate = getRequisitionNodesToUpdate(nodeToCategoryMappings, requisitionManager);

        return requisitionNodesToUpdate;
    }

    private List<RequisitionNode> getRequisitionNodesToUpdate(List<NodeToCategoryMapping> nodeToCategoryMappings, RestRequisitionManager requisitionManager) {

        List<RequisitionNode> reqNodesToUpdate = new ArrayList<RequisitionNode>();

        for (NodeToCategoryMapping node2Category : nodeToCategoryMappings) {
            RequisitionNode requisitionNode = requisitionManager.getReqisitionNode(node2Category.getNodeLabel());
            if (requisitionNode != null) {

                //add all set categories
                Integer initalAmountOfCategories = requisitionNode.getCategories().size();
                for (RequisitionCategory addCategory : node2Category.addCategories) {
                    requisitionNode.putCategory(addCategory);
                }

                //remove all not set categories
                Integer afterAddingAmountOfCategories = requisitionNode.getCategories().size();
                for (RequisitionCategory removeCategory : node2Category.removeCategories) {
                    requisitionNode.deleteCategory(removeCategory);
                }
                Integer afterRemoveAmountOfCategories = requisitionNode.getCategories().size();

                //compare amount of categories per step to identify changed requisition nodes
                if (initalAmountOfCategories.equals(afterAddingAmountOfCategories) && afterAddingAmountOfCategories.equals(afterRemoveAmountOfCategories)) {
                    logger.info("RequisitionNode '{}' has no updates", requisitionNode.getNodeLabel());
                } else {
                    logger.info("RequisitionNode '{}' has updates", requisitionNode.getNodeLabel());
                    reqNodesToUpdate.add(requisitionNode);
                }

            } else {
                logger.info("RequisitionNode '{}' is unknowen on the system", node2Category.nodeLabel);
            }
        }

        for (RequisitionNode reqNode : reqNodesToUpdate) {
            logger.info("Node to change '{}'", reqNode.getNodeLabel());
        }

        return reqNodesToUpdate;
    }


}
