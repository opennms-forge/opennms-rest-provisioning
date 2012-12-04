/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org> http://www.opennms.org/ http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.forge.provisioningrestclient.api;

import com.sun.jersey.client.apache.ApacheHttpClient;
import org.opennms.forge.restclient.api.RestRequisitionProvider;
import org.opennms.forge.restclient.utils.RestConnectionParameter;
import org.opennms.forge.restclient.utils.RestHelper;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>RequisitionManager class.</p>
 *
 * @author <a href="mailto:markus@opennms.org">Markus Neumann</a>*
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 * @version 1.0-SNAPSHOT
 * @since 1.0-SNAPSHOT
 *        <p/>
 *        TODO tak: missing structure that adds the requisition for the nodes
 */
public class RequisitionManager {

    private ApacheHttpClient m_httpClient = null;
    private Map<String, RequisitionNode> m_reqNodesByLabel = new HashMap<String, RequisitionNode>();
    private RestRequisitionProvider m_restRequisitionProvider;
    private Requisition m_requisition;

    /**
     * <p>RequisitionManager</p>
     * <p/>
     * Constructor to handle the data structure for an OpenNMS provisioning ReST service.
     *
     * @param restConnectionParameter Connection parameter for HTTP ReST cliet
     */
    public RequisitionManager(RestConnectionParameter restConnectionParameter) {
        this.m_httpClient = RestHelper.createApacheHttpClient(restConnectionParameter);
        m_restRequisitionProvider = new RestRequisitionProvider(restConnectionParameter);
    }

    /**
     * <p>loadNodesByLabelForRequisition</p>
     * <p/>
     * Load nodes from a given requisition identified by foreign source and store the nodes
     * in internal requisition with a filter or limit as parameter string.
     *
     * @param foreignSource Name of the requisition as {@link java.lang.String}
     * @param parameter     Filter or limit parameter as {@link java.lang.String}
     */
    public void loadNodesByLabelForRequisition(String foreignSource, String parameter) {
        m_requisition = m_restRequisitionProvider.getRequisition(foreignSource, parameter);
        for (RequisitionNode reqNode : m_requisition.getNodes()) {
            m_reqNodesByLabel.put(reqNode.getNodeLabel(), reqNode);
        }
    }

    /**
     * <p>getRequisitionNode</p>
     * <p/>
     * Get a requisition node identified by node label
     *
     * @param nodeLabel Node label as {@link java.lang.String}
     * @return Requisition node as @{link org.opennms.netmgt.provision.persist.requisition.RequisitionNode}
     */
    public RequisitionNode getRequisitionNode(String nodeLabel) {
        return m_reqNodesByLabel.get(nodeLabel);
    }

    /**
     * <p>getRequisition</p>
     * <p/>
     * Get the whole previously loaded requisition.
     *
     * @return Requisition loaded into the requisition manager as {@link org.opennms.netmgt.provision.persist.requisition.Requisition}
     */
    public Requisition getRequisition() {
        return m_requisition;
    }

    /**
     * <p>getRestRequisitionProvider</p>
     * <p/>
     * Get the ReST requisition provider to use ReST calls
     *
     * @return ReST requisition provider as {@link org.opennms.forge.restclient.api.RestRequisitionProvider}
     */
    public RestRequisitionProvider getRestRequisitionProvider() {
        return this.m_restRequisitionProvider;
    }
}