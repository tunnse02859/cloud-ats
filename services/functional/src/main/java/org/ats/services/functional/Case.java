/**
 * 
 */
package org.ats.services.functional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ats.services.functional.action.AbstractAction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class Case extends AbstractTemplate {
  
  private String name;
  
  private boolean checkDataDriven;
  
  private List<AbstractAction> actions = new ArrayList<AbstractAction>();
  
  public Case(String name, boolean checkDataDriven) {
    this.name = name;
    this.checkDataDriven = checkDataDriven;
  }
  
  public String getName() {
    return name;
  }
  
  public Case addAction(AbstractAction... actions) {
    for (AbstractAction action : actions) {
      if (action == null) continue;
      this.actions.add(action);
    }
    return this;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    
    if(checkDataDriven) {
      
      String DATA_PATH = "C:\\Users\\nambv2\\Desktop\\data.json";
      ObjectMapper obj = new ObjectMapper();
      JsonNode rootNode;
      
      rootNode = obj.readTree(new File(DATA_PATH));
      JsonNode json = rootNode.get(0);
      Iterator<Entry<String, JsonNode>> nodeIterator = json.fields();
      
      sb.append("\n");
      sb.append("  @Test (dataProvider = \"LoadData\")\n");
      sb.append("  public void ").append(name).append("(JsonNode data) throws Exception {\n");
      
      while (nodeIterator.hasNext()) {
        Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
        String a = "String "+entry.getKey()+" = data.get(\""+entry.getKey().toString()+"\").toString().split(\"\\\"\")[1];\n";
        sb.append(a);

     }
      
    } else {
    
      sb.append("\n");
      sb.append("  @Test\n");
      sb.append("  public void ").append(name).append("() throws Exception {\n");
    }
     for (AbstractAction action : actions) {
       sb.append("    ");
       sb.append(action.transform());
       sb.append("\n");
     }
     sb.append("  }\n");
     return sb.toString();
  }

  @Override
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
