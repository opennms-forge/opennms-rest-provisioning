/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import de.dertak.opennms.restclientapi.helper.RestHelper;
import de.dertak.opennms.restclientapi.manager.RestRequisitionManager;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Markus@OpenNMS.org
 */
public class NodeToCategoryMappingTest {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(NodeToCategoryMappingTest.class);
    private SpreadsheetReader reader;
    private RestRequisitionManager requisitionManager;

    private String baseUrl = "http://localhost:8980/opennms/";
    private String username = "admin";
    private String password = "admin";

    @Before
    public void setup() {
        reader = new SpreadsheetReader();
        requisitionManager = new RestRequisitionManager(RestHelper.createApacheHttpClient(username, password), baseUrl);
        requisitionManager.loadNodesByLableForAllRequisitions();
    }

    @Test
    public void testReqNodeMerge() {
        List<NodeToCategoryMapping> nodeToCategoryMappings = reader.getNodeToCategoryMappingsFromFile(new File("/home/tak/test.ods"));
        logger.info("Got '{}' NodeCategoryChanges.",nodeToCategoryMappings.size());
        for (NodeToCategoryMapping nodeToCategoryMapping : nodeToCategoryMappings) {
            logger.info("NodeToCategoryMapping for '{}' found addCategory size is '{}' found remove Category size is '{}'", nodeToCategoryMapping.getNodeLabel(), nodeToCategoryMapping.getAddCategories().size(), nodeToCategoryMapping.getRemoveCategories().size());
            RequisitionNode reqNode = requisitionManager.getReqisitionNode(nodeToCategoryMapping.getNodeLabel());
            for(RequisitionCategory category : nodeToCategoryMapping.getAddCategories()) {
                reqNode.putCategory(category);
            }
            logger.info("RequisitionNode '{}' '{}'", reqNode.getNodeLabel(), reqNode);
        }
    }
}
