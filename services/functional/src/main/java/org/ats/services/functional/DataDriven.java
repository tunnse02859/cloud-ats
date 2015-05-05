/**
 * 
 */
package org.ats.services.functional;

import java.io.IOException;

/**
 * @author NamBV2
 *
 * Apr 24, 2015
 */

@SuppressWarnings("serial")
public class DataDriven extends AbstractTemplate {

  public DataDriven(String name,String pathData) {
    this.put("name", name);
    this.put("data_path", pathData);
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public String getDataPath() {
    return this.getString("data_path");
  }
  
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    String path = getDataPath();
    sb.append("@DataProvider(name = \"");
    sb.append(getName());
    sb.append("\")\n");
    sb.append("  public static Object[][] ").append(getName()).append("() throws Exception {\n");
    sb.append("    String DATA_PATH = \"");
    sb.append(path);
    sb.append("\";\n");
    sb.append("    ObjectMapper obj = new ObjectMapper();\n");
    sb.append("    JsonNode rootNode;\n");
    sb.append("    List<JsonNode> listData = new ArrayList<JsonNode>();\n");
    sb.append("\n");
    sb.append("    rootNode = obj.readTree(new File(DATA_PATH));\n");
    sb.append("    for(JsonNode json:rootNode) {\n");
    sb.append("      listData.add(json);\n}\n");
    sb.append("\n");
    sb.append("    JsonNode[][] objData = new JsonNode[listData.size()][];\n");
    sb.append("    for(int i=0; i<listData.size(); i++) {\n");
    sb.append("      objData[i] = new JsonNode[]{listData.get(i)};\n}\n");
    sb.append("    return objData;\n}");
    
    return sb.toString();
  }
}
