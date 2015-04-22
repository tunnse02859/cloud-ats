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
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class Case extends AbstractTemplate {
  
  private String name;
  
  private List<AbstractAction> actions = new ArrayList<AbstractAction>();
  
  public Case(String name) {
    this.name = name;
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
    sb.append("\n");
    sb.append("  @Test\n");
    sb.append("  public void ").append(name).append("() throws Exception {\n");
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
