/**
 * 
 */
package org.ats.services.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ats.services.functional.action.AbstractAction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBList;
import com.mongodb.util.JSON;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class Case extends AbstractTemplate {
  
  private List<JsonNode> actions = new ArrayList<JsonNode>();
  
  @Inject
  private ActionFactory actionFactory;
  
  @Inject
  private Case(@Assisted("name") String name, @Assisted("dataProvider") String dataProvider, @Assisted("dataProviderName") String dataProviderName) {
    this.put("name", name);
    this.put("data_provider", dataProvider);
    this.put("data_provider_name", dataProviderName);
    this.put("actions",  null);
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public Case addAction(JsonNode... actions) {
    for (JsonNode action : actions) {
      if (action == null) continue;
      this.actions.add(action);
    }
    
    BasicDBList list = new BasicDBList();
    for (JsonNode action : this.actions) {
      list.add(JSON.parse(action.toString()));
    }
    this.put("actions",  list);
    return this;
  }
  
  public List<JsonNode> getActions() {
    return Collections.unmodifiableList(actions);
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    boolean isUseDataProvider = this.get("data_provider") != null;
    
    if(isUseDataProvider) {
      ObjectMapper obj = new ObjectMapper();
      JsonNode rootNode;
      rootNode = obj.readTree(this.getString("data_provider"));

      JsonNode data = rootNode.get(0);
      Iterator<Entry<String, JsonNode>> nodeIterator = data.fields();
      
      sb.append("\n");
      sb.append("  @Test (dataProvider = \"").append(this.get("data_provider_name")).append("\")\n");
      sb.append("  public void ").append(getName()).append("(JsonNode data) throws Exception {\n");
      
      while (nodeIterator.hasNext()) {
        Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodeIterator.next();
        String a = "    String "+entry.getKey()+" = data.get(\""+entry.getKey().toString()+"\").toString().split(\"\\\"\")[1];\n";
        sb.append(a);
     }
      
    } else {
      sb.append("\n");
      sb.append("  @Test\n");
      sb.append("  public void ").append(getName()).append("() throws Exception {\n");
    }
    
    for (JsonNode json : actions) {
      AbstractAction action = actionFactory.createAction(json);
      if (action == null) continue;
      sb.append("    ");
      sb.append(action.transform());
      sb.append("\n");
    }
    
    sb.append("  }");
    return sb.toString();
  }
}
