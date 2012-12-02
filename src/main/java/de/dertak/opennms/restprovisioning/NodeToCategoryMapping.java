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

import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus@OpenNMS.com
 */
public class NodeToCategoryMapping {
    String nodeLabel;
    List<RequisitionCategory> addCategories = new ArrayList<RequisitionCategory>();
    List<RequisitionCategory> removeCategories = new ArrayList<RequisitionCategory>();

    public NodeToCategoryMapping(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public NodeToCategoryMapping(String nodeLabel, List<RequisitionCategory> addCategories, List<RequisitionCategory> removeCategories) {
        this.nodeLabel = nodeLabel;
        this.addCategories = addCategories;
        this.removeCategories = removeCategories;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public List<RequisitionCategory> getAddCategories() {
        return addCategories;
    }

    public void setAddCategories(List<RequisitionCategory> addCategories) {
        this.addCategories = addCategories;
    }

    public List<RequisitionCategory> getRemoveCategories() {
        return removeCategories;
    }

    public void setRemoveCategories(List<RequisitionCategory> removeCategories) {
        this.removeCategories = removeCategories;
    }

    @Override
    public String toString() {
        return "NodeToCategoryMapping{" + "nodeLabel=" + nodeLabel + ", addCategories=" + addCategories + ", removeCategories=" + removeCategories + '}';
    }
}
