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

public class Starter {

    private static Logger logger = LoggerFactory.getLogger(Starter.class);
    private final static File ODS_FILE = new File("/home/tak/test.ods");

    private final static String BASE_URL = "http://localhost:8980/opennms/";
    private final static String USER_NAME = "admin";
    private final static String PASSWORD = "admin";

//    private final static String BASE_URL = "http://demo.opennms.com/opennms/";
//    private final static String USER_NAME = "demo";
//    private final static String PASSWORD = "demo";

    private final static ApacheHttpClient HTTP_CLIENT = RestHelper.createApacheHttpClient(USER_NAME, PASSWORD);

    public static void main(String[] args) {
        logger.info("OpenNMS Rest Provisioning");

        //create and prepare RestRequisitionManager
        RestRequisitionManager requisitionManager = new RestRequisitionManager(HTTP_CLIENT, BASE_URL);
        requisitionManager.loadNodesByLableForAllRequisitions();

        //read node to category mappings from spreadsheet
        SpreadsheetReader spreadsheetReader = new SpreadsheetReader();
        List<NodeToCategoryMapping> nodeToCategoryMappings = spreadsheetReader.getNodeToCategoryMappingsFromFile(ODS_FILE);
        
        List<RequisitionNode> requisitionNodesToUpdate = getRequisitionNodesToUpdate(nodeToCategoryMappings, requisitionManager);
        
        logger.info("Thanks for computing with OpenNMS!");
    }

    private static List<RequisitionNode> getRequisitionNodesToUpdate(List<NodeToCategoryMapping> nodeToCategoryMappings, RestRequisitionManager requisitionManager) {
      
        List<RequisitionNode> reqNodesToUpdate = new ArrayList<RequisitionNode>();
       
        for(NodeToCategoryMapping node2Category : nodeToCategoryMappings) {
            RequisitionNode requisitionNode = requisitionManager.getReqisitionNode(node2Category.getNodeLabel());
            if(requisitionNode != null) {

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
                if(initalAmountOfCategories.equals(afterAddingAmountOfCategories) && afterAddingAmountOfCategories.equals(afterRemoveAmountOfCategories)) {
                    logger.info("RequisitionNode '{}' has no updates", requisitionNode.getNodeLabel());
                } else {
                    logger.info("RequisitionNode '{}' has updates", requisitionNode.getNodeLabel());
                    reqNodesToUpdate.add(requisitionNode);
                }
                
            } else {
                logger.info("RequisitionNode '{}' is unknowen on the system", node2Category.nodeLabel);
            }
        }

        for(RequisitionNode reqNode : reqNodesToUpdate) {
            logger.info("Node to change '{}'", reqNode.getNodeLabel());
        }

        return reqNodesToUpdate;
    }
}