/**
 * 
 */
package org.ats.services.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ats.services.functional.action.AbstractAction;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author NamBV2
 *
 * Apr 24, 2015
 */

@SuppressWarnings("serial")
public class DataDriven extends AbstractTemplate{

  private String name;
  
  private String pathData;
  
  private List<AbstractAction> actions = new ArrayList<AbstractAction>();
  
  public DataDriven(String name,String pathData) {
    this.name = name;
    this.pathData = pathData;
  }
  
  public String getName() {
    return name;
  }
  
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    String path = pathData.replace("/", "\\\\");
    sb.append("@DataProvider(name = \"");
    sb.append(name);
    sb.append("\")\n");
    sb.append("public static Object[][] loadData() throws JsonProcessingException, IOException {\n");
    sb.append("String DATA_PATH = ");
    sb.append(path);
    sb.append(";\n");
    sb.append("ObjectMapper obj = new ObjectMapper();\n");
    sb.append("JsonNode rootNode;\n");
    sb.append("List<JsonNode> listData = new ArrayList<JsonNode>();\n");
    sb.append("\n");
    sb.append("rootNode = obj.readTree(new File(DATA_PATH));\n");
    sb.append("for(JsonNode json:rootNode) {\n");
    sb.append("   listData.add(json);\n}\n");
    sb.append("\n");
    sb.append("JsonNode[][] objData = new JsonNode[listData.size()][];\n");
    sb.append("for(int i=0; i<listData.size(); i++) {\n");
    sb.append("   objData[i] = new JsonNode[]{listData.get(i)};\n}\n");
    sb.append("return objData;\n}");
    
    return sb.toString();
  }

  public DBObject toJson() {
    BasicDBObject obj = new BasicDBObject("name", name);
    BasicDBList list = new BasicDBList();
    for (AbstractAction action : actions) {
      list.add(action.toJson());
    }
    obj.put("actions",  list);
    return obj;
  }

}
