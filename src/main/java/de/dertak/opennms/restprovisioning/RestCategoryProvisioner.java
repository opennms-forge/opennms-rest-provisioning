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
import de.dertak.opennms.restclientapi.manager.RestRequisitionManager;
import de.dertak.opennms.restprovisioning.helper.SpreadsheetLayouter;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 */
class RestCategoryProvisioner {

    /**
     * Logging
     */
    private static Logger logger = LoggerFactory.getLogger(RestCategoryProvisioner.class);

    /**
     * Base URL for OpenNMS ReST services
     */
    private String m_baseUrl;

    /**
     * ODS file with node and category association
     */
    private File m_odsFile;

    /**
     * Name of the provisioning requisition
     */
    private String m_foreignSource;

    /**
     * Default do not apply the new categories on the node, give a feedback for
     * sanity check first
     */
    private boolean m_apply = false;

    /**
     * Web client to handle the ReST calls
     */
    private ApacheHttpClient m_httpClient;

    /**
     * Contains the nodes from OpenNMS and provides access to the requisition node
     */
    private RestRequisitionManager m_restRequisitionManager;

    /**
     * Constructor to initialize the ReST category provisioner.
     *
     * @param baseUrl         Base URL to OpenNMS ReST services as {@link java.lang.String}
     * @param httpClient      Client to handle ReST calls as {@link com.sun.jersey.client.apache.ApacheHttpClient}
     * @param odsFile         File handle to ODS file with nodes and categories which have to be set as {@link java.io.File}
     * @param foreignSource   Name of the foreign source which has to be updated
     * @param apply           Flag for preview or directly apply changes in OpenNMS and synchronize the OpenNMS database
     */
    public RestCategoryProvisioner(String baseUrl, ApacheHttpClient httpClient, File odsFile, String foreignSource, Boolean apply) {
        this.m_baseUrl = baseUrl;
        this.m_odsFile = odsFile;
        this.m_foreignSource = foreignSource;
        this.m_apply = apply;
        this.m_httpClient = httpClient;
        this.m_restRequisitionManager = new RestRequisitionManager(m_httpClient, m_baseUrl);
    }

    /**
     * <p>getRequisitionNodesToUpdate</p>
     * <p/>
     * Get a list of all requisition nodes and apply all categories which are defined in the
     * ODS sheet.
     *
     * @return List of requisition nodes as {@link java.util.List<RequisitionNode>}
     */
    public List<RequisitionNode> getRequisitionNodesToUpdate() {
        //create and prepare RestRequisitionManager
        m_restRequisitionManager.loadNodesByLabelForRequisition(m_foreignSource, "");

        //read node to category mappings from spreadsheet
        SpreadsheetReader spreadsheetReader = new SpreadsheetReader();
        List<NodeToCategoryMapping> nodeToCategoryMappings = spreadsheetReader.getNodeToCategoryMappingsFromFile(m_odsFile, m_foreignSource);

        List<RequisitionNode> requisitionNodesToUpdate = getRequisitionNodesToUpdate(nodeToCategoryMappings, m_restRequisitionManager);

        return requisitionNodesToUpdate;
    }

    /**
     * <p>getRequisitionNodesToUpdate</p>
     * <p/>
     * Private method to build the list of the applied requisition nodes. Remove and add all categories defined by the nodeToCategoryMappings and
     * return a list of requisition nodes.
     *
     * @param nodeToCategoryMappings Mapping from nodes and surveillance categories {@link java.util.List<RequisitionNode>}
     * @param restRequisitionManager Requisition manager handles the node representation from OpenNMS
     * @return List of nodes which has to be provisioned as {@link java.util.List<RequisitionNode>}
     */
    private List<RequisitionNode> getRequisitionNodesToUpdate(List<NodeToCategoryMapping> nodeToCategoryMappings, RestRequisitionManager restRequisitionManager) {

        List<RequisitionNode> reqNodesToUpdate = new ArrayList<RequisitionNode>();

        for (NodeToCategoryMapping node2Category : nodeToCategoryMappings) {
            RequisitionNode requisitionNode = restRequisitionManager.getRequisitionNode(node2Category.getNodeLabel());
            if (requisitionNode != null) {

                //add all set categories
                Integer initialAmountOfCategories = requisitionNode.getCategories().size();
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
                if (initialAmountOfCategories.equals(afterAddingAmountOfCategories) && afterAddingAmountOfCategories.equals(afterRemoveAmountOfCategories)) {
                    logger.info("RequisitionNode '{}' has no updates", requisitionNode.getNodeLabel());
                } else {
                    logger.info("RequisitionNode '{}' has updates", requisitionNode.getNodeLabel());
                    reqNodesToUpdate.add(requisitionNode);
                }

            } else {
                logger.info("RequisitionNode '{}' is unknown on the system", node2Category.nodeLabel);
            }
        }

        // Logging to see for which node new surveillance categories will be set
        for (RequisitionNode reqNode : reqNodesToUpdate) {
            logger.info("Node to change '{}'", reqNode.getNodeLabel());
        }

        return reqNodesToUpdate;
    }

    /**
     * <p>generateOdsFile</p>
     * <p/>
     * Generate an ODS file from an OpenNMS provisioning requisition identified by name.
     * <p/>
     * TODO: Read all categories
     * TODO: Read all nodes, labels and foreign-ids
     *
     * @param foreignSource Name of provisioning requisition to generate the ODS file
     */
    public void generateOdsFile(String foreignSource) {
        // read the requisition by using the RestRequisitionManager
        m_restRequisitionManager.loadNodesByLabelForRequisition(foreignSource, "");
        Requisition requisition = m_restRequisitionManager.getRequisition();

        SpreadsheetReader spreadsheetReader = new SpreadsheetReader();
        File generatedOdsFile = spreadsheetReader.getSpeadsheetFromRequisition(requisition);
        File formattedOdsFile = SpreadsheetLayouter.layoutGeneratedOdsFile(generatedOdsFile);


        // read all categories
        // read all nodes, labels and foreign-ids
    }
}
