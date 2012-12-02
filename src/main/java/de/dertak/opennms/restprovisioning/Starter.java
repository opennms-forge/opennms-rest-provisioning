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
import de.dertak.opennms.restclientapi.helper.OnmsRestConnectionParameter;
import de.dertak.opennms.restclientapi.helper.RestConnectionParameter;
import de.dertak.opennms.restclientapi.helper.RestHelper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @author Markus@OpenNMS.org
 */

public class Starter {

    private static Logger logger = LoggerFactory.getLogger(Starter.class);

    @Option(name = "-baseurl", required = true, usage = "baseurl of the system to work with; demo.opennms.com/opennms/")
    private String baseUrl = "http://localhost:8980/opennms/";

    @Option(name = "-username", required = true, usage = "username to work with the system")
    private String username = "admin";

    @Option(name = "-password", required = true, usage = "password to work with the system")
    private String password = "admin";

    @Option(name = "-odsFile", required = true, usage = "path to the odsFile to read from")
    private String odsFilePath;

    @Option(name = "-requisition", required = true, usage = "name of the requisition to work with")
    private String requisition;

    @Option(name = "-apply", usage = "if this option is set, changes will be applied to the remote system.")
    private boolean apply = false;

    @Option(name = "-generateOds", usage = "if this option is set, just a ods file with the data from the remote system will be created in temp folder.")
    private boolean generateOds = false;

    /**
     * Set maximal terminal width for line breaks
     */
    private final int TERMINAL_WIDTH = 120;

    private RestConnectionParameter m_resRestConnectionParameter = null;

    public static void main(String[] args) throws IOException {
        new Starter().doMain(args);
    }

    public void doMain(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);

        parser.setUsageWidth(TERMINAL_WIDTH);

        try {
            m_resRestConnectionParameter = new OnmsRestConnectionParameter(baseUrl, username, password);
        } catch (MalformedURLException e) {
            logger.error("Base URL '{}' is not valid. Error message '{}'", baseUrl, e.getMessage());
            System.exit(1);
        }

        ApacheHttpClient httpClient = RestHelper.createApacheHttpClient(username, password);

        logger.info("OpenNMS Category Provisioning");
        try {
            parser.parseArgument(args);
            File odsFile = new File(odsFilePath);
            if (odsFile.exists() && odsFile.canRead()) {
                RestCategoryProvisioner restCategoryProvisioner = new RestCategoryProvisioner(baseUrl, httpClient, odsFile, requisition, apply);
                if (generateOds) {
                    restCategoryProvisioner.generateOdsFile(requisition);
                }
                else {
                    restCategoryProvisioner.getRequisitionNodesToUpdate();
                }
            }
            else {
                logger.info("The odsFile '{}' dose not exist or is not readable, sorry.", odsFilePath);
            }
        } catch (CmdLineException ex) {
            parser.printUsage(System.err);
        }

        logger.info("Thanks for computing with OpenNMS!");
    }
}