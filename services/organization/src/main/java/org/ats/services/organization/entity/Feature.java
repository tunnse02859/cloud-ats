/**
 * 
 */
package org.ats.services.organization.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.ats.services.data.common.Reference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
@SuppressWarnings("serial")
public class Feature extends AbstractEntity<Feature> {
  
  public static final Reference<Feature> ANY = new Reference<Feature>("*") {
    @Override
    public Feature get() { return null; }
  };

  @Inject
  Feature(@Assisted String name) {
    this.put("_id", name);
    this.put("created_date", new Date());
    this.setActive(true);
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public String getName() {
    return getId();
  }
  
  public void addAction(Action... actions) {
    Object obj = this.get("actions");
    BasicDBList list = obj == null ? new BasicDBList() : (BasicDBList) obj;
    for (Action action : actions) {
      list.add(action);
    }
    this.put("actions", list);
  }
  
  public void removeAction(Action action) {
    Object obj = this.get("actions");
    if (obj == null) return;
    
    BasicDBList actions = (BasicDBList) obj;
    actions.remove(action);
    this.put("actions", actions);
  }
  
  public boolean hasAction(Action action) {
    if (action == Action.ANY) return true;
    Object obj = this.get("actions");
    return obj == null ? false : ((BasicDBList) obj).contains(action);
  }
  
  public List<Action> getActions() {
    Object obj = this.get("actions");
    if (obj == null) return Collections.emptyList();
    
    BasicDBList actions = (BasicDBList) obj;
    List<Action> list = new ArrayList<Action>();
    for (int i = 0; i < actions.size(); i++) {
      list.add(new Action(((BasicDBObject)actions.get(i)).getString("_id")));
    }
    
    return Collections.unmodifiableList(list);
  }
  
  public static class Action extends BasicDBObject {
    
    public static final Action ANY = new Action("*");
    
    public Action(String name) {
      this.put("_id", name);
    }
    
    public String getId() {
      return this.getString("_id");
    }
    
    public String getName() {
      return getId();
    }
    
    public void setDescription(String desc) {
      this.put("desc", desc);
    }
    
    public String getDescription() {
      return this.getString("desc");
    }
  }
}
