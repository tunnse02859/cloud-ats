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

import javax.annotation.Nullable;

import org.ats.services.datadriven.DataDriven;
import org.ats.services.datadriven.DataDrivenReference;
import org.ats.services.functional.action.AbstractAction;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
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
  private ReferenceFactory<DataDrivenReference> drivenRefFactory;
  
  @Inject
  private Case(@Assisted("name") String name, @Nullable @Assisted("dataDriven") DataDrivenReference ref) {
    this.put("name", name);
    this.put("data_driven", ref != null ? ref.toJSon() : null);
    this.put("actions",  null);
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public DataDrivenReference getDataDriven() {
    Object obj = this.get("data_driven");
    if (obj == null) return null;
    return drivenRefFactory.create(((BasicDBObject) obj).getString("_id"));
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
    boolean isUseDataProvider = getDataDriven() != null;
    
    if(isUseDataProvider) {
      
      DataDriven dataDriven = getDataDriven().get();
      
      ObjectMapper obj = new ObjectMapper();
      JsonNode rootNode;
      rootNode = obj.readTree(dataDriven.getDataSource());

      JsonNode data = rootNode.get(0);
      Iterator<Entry<String, JsonNode>> nodeIterator = data.fields();
      
      sb.append("\n");
      sb.append("  @Test (dataProvider = \"").append(dataDriven.getName()).append("\")\n");
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
