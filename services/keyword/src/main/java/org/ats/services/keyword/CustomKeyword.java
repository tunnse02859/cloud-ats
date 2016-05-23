/**
 * 
 */
package org.ats.services.keyword;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jun 24, 2015
 */
@SuppressWarnings("serial")
public class CustomKeyword extends BasicDBObject {
  
  private List<JsonNode> actions = new ArrayList<JsonNode>();
  
  @Inject
  private CustomKeyword(@Assisted("projectId") String projectId, @Assisted("name") String name, @Assisted("creator") String creator) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("project_id", projectId);
    this.put("name", name);
    this.put("created_date", new Date());
    this.put("steps", null);
    this.put("creator", creator);
    
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public void setName(String name) {
    this.put("name", name);
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public void setCreator(String creator) {
    this.put("creator", creator);
  }
  
  public String getCreator() {
    return this.getString("creator");
  }
  
  public String getProjectId() {
    return this.getString("project_id");
  }
  
  public CustomKeyword addAction(JsonNode... actions) {
    for (JsonNode action : actions) {
      if (action == null) continue;
      this.actions.add(action);
    }
    
    BasicDBList list = new BasicDBList();
    for (JsonNode action : this.actions) {
      list.add(JSON.parse(action.toString()));
    }
    this.put("steps",  list);
    return this;
  }
  
  public List<JsonNode> getActions() {
    return Collections.unmodifiableList(actions);
  }
  
  public void clearActions() {
    this.remove("steps");
    this.actions.clear();
  }

}
