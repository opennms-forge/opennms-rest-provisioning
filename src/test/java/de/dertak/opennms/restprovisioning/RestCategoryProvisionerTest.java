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

import com.sun.jersey.client.apache.ApacheHttpClient;
import de.dertak.opennms.restclientapi.helper.RestHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import org.junit.Ignore;

/**
 *
 * @author Markus@OpenNMS.org
 */

public class RestCategoryProvisionerTest {

    private static Logger logger = LoggerFactory.getLogger(RestCategoryProvisionerTest.class);

    private String m_baseUrl = "http://localhost:8980/opennms/";

    private String m_userName = "admin";

    private String m_password = "admin";

    private File m_odsFile = new File("/home/tak/test.ods");

    private String m_requisition = "RestProvisioningTest";

    private boolean m_apply = false;

    private RestCategoryProvisioner m_provider;

    @Before
    public void setUp() {
        ApacheHttpClient apacheHttpClient = RestHelper.createApacheHttpClient(m_userName, m_password);
        m_provider = new RestCategoryProvisioner(m_baseUrl, apacheHttpClient, m_odsFile, m_requisition, m_apply);
    }

    @Ignore
    @Test
    public void testDoThings() {
        List<RequisitionNode> requisitionNodesToUpdate = m_provider.getRequisitionNodesToUpdate();
        Assert.assertEquals(4, requisitionNodesToUpdate.size());
    }
}
