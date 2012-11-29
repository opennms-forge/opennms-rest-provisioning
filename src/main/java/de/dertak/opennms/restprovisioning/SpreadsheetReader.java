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

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.odftoolkit.odfdom.doc.table.OdfTableColumn;
import org.odftoolkit.odfdom.doc.table.OdfTableRow;
import org.odftoolkit.odfdom.type.Color;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Markus@OpenNMS.org
 */
public class SpreadsheetReader {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetReader.class);

    public List<NodeToCategoryMapping> getNodeToCategoryMappingsFromFile(File odsFile) {
        List<NodeToCategoryMapping> nodes = new LinkedList<NodeToCategoryMapping>();
        List<String> categories = new LinkedList<String>();
        if (odsFile.exists() && odsFile.canRead()) {
            try {
                OdfSpreadsheetDocument spreadsheet = OdfSpreadsheetDocument.loadDocument(odsFile);
                OdfTable table = spreadsheet.getTableByName("Thresholds");
                OdfTableColumn nodeColumn = table.getColumnByIndex(0);
                OdfTableRow categoryRow = table.getRowByIndex(0);

                //Build a list of all Categories
                int categoryIndex = 1;
                while (!categoryRow.getCellByIndex(categoryIndex).getDisplayText().equals("")) {
                    categories.add(categoryRow.getCellByIndex(categoryIndex).getDisplayText());
                    categoryIndex++;
                }

                //Build a list of all Nodes with AddCategories and RemoveCategories
                int rowIndex = 1;
                while (!nodeColumn.getCellByIndex(rowIndex).getDisplayText().equals("")) {
                    NodeToCategoryMapping node = new NodeToCategoryMapping(nodeColumn.getCellByIndex(rowIndex).getDisplayText());

                    for(int cellId = 1; cellId <= categories.size(); cellId++) {
                        if (table.getRowByIndex(rowIndex).getCellByIndex(cellId).getDisplayText().equals("")) {
                        table.getRowByIndex(rowIndex).getCellByIndex(cellId).setCellBackgroundColor(Color.RED);
                            node.getRemoveCategories().add(categories.get(cellId -1));
                            logger.debug("Node '{}' found removeCategory '{}'", node.getNodeLabel(),categories.get(cellId -1) );
                        } else {
                            table.getRowByIndex(rowIndex).getCellByIndex(cellId).setCellBackgroundColor(Color.GREEN);
                            node.getAddCategories().add(categories.get(cellId -1));
                            logger.debug("Node '{}' found addCategory    '{}'", node.getNodeLabel(),categories.get(cellId -1) );
                        }
                    }
                    nodes.add(node);
                    rowIndex++;
                }
                spreadsheet.save(new File(System.getProperty("java.io.tmpdir")+ File.separator + "newFile.ods"));
            } catch (Exception ex) {
                logger.error("Reading odsFile went wrong", ex);
            }

        } else {
            logger.error("OdsFile '{}' dose not exist or is not readable.", odsFile);
        }

        return nodes;
    }
}