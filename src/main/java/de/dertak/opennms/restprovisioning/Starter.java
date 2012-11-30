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

import java.io.File;
import java.io.IOException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Starter {

    private static Logger logger = LoggerFactory.getLogger(Starter.class);

    @Option(name = "-baseurl", required = true, usage = "baseurl of the system to work with; demo.opennms.com/opennms/")
    private String baseUrl;

    @Option(name = "-username", required = true, usage = "username to work with the system")
    private String userName;

    @Option(name = "-password", required = true, usage = "password to work with the system")
    private String password;

    @Option(name = "-odsFile", required = true, usage = "path to the odsFile to read from")
    private String odsFilePath;

    @Option(name = "-requisition", required = true, usage = "name of the requisition to work with")
    private String requisition;

    @Option(name = "-apply", usage = "just if this option is set, changes will be applied to the remote system.")
    private boolean apply = false;

    public static void main(String[] args) throws IOException {
        new Starter().doMain(args);
    }

    public void doMain(String[] args) {

        CmdLineParser parser = new CmdLineParser(this);

        parser.setUsageWidth(120);
        logger.info("OpenNMS Category Provisioning");
        try {
            parser.parseArgument(args);
                File odsFile = new File(odsFilePath);
                if(odsFile.exists() && odsFile.canRead()) {
                    RestCategoryProvisioner restCategoryProvisioner = new RestCategoryProvisioner(baseUrl, userName, password, odsFile, requisition, apply);
                    restCategoryProvisioner.doThings();
                }else {
                    logger.info("The odsFile '{}' dose not exist or is not readable, sorry.", odsFilePath);
                }
        } catch (CmdLineException ex) {
            parser.printUsage(System.err);
        }

        logger.info("Thanks for computing with OpenNMS!");
    }
}