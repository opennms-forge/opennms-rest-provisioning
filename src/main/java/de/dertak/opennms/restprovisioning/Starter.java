package de.dertak.opennms.restprovisioning;

import com.sun.jersey.client.apache.ApacheHttpClient;
import de.dertak.opennms.restclientapi.RestRequisitionProvider;
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

        SpreadsheetReader spreadsheetReader = new SpreadsheetReader();
        List<NodeToCategoryMapping> nodeToCategoryMappings = spreadsheetReader.getNodeToCategoryMappingsFromFile(ODS_FILE);

        List<RequisitionNode> reqNodesToChange = new ArrayList<RequisitionNode>();
        List<NodeToCategoryMapping> nodesUnknowen = new ArrayList<NodeToCategoryMapping>();

        for(NodeToCategoryMapping node2Category : nodeToCategoryMappings) {
            RequisitionNode reqisitionNode = requisitionManager.getReqisitionNode(node2Category.getNodeLabel());
            if(reqisitionNode != null) {
                List<RequisitionCategory> categories = reqisitionNode.getCategories();
                for(String addCategorie : node2Category.addCategories) {
                    categories.add(new RequisitionCategory(addCategorie));
                }
                for(String removeCategorie : node2Category.removeCategories) {
                    categories.remove(new RequisitionCategory(removeCategorie));
                }
                //Not every node in this list has realy a change....
                reqNodesToChange.add(reqisitionNode);
            } else {
                nodesUnknowen.add(node2Category);
            }
        }

        for(RequisitionNode reqNode : reqNodesToChange) {
            logger.info("Node to change '{}'", reqNode.getNodeLabel());
        }

        for(NodeToCategoryMapping nodeToCategoryMapping : nodesUnknowen) {
            logger.info("Node unknowen on the remote system '{}'", nodeToCategoryMapping.getNodeLabel());
        }

        for(RequisitionNode reqNode : reqNodesToChange) {
            RestRequisitionProvider.updateRequisionNodeCategories(HTTP_CLIENT, BASE_URL, "",reqNode);
        }
        logger.info("Thanks for computing with OpenNMS!");
    }
}
