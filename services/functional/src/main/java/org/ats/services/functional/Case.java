/**
 * 
 */
package org.ats.services.functional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ats.services.functional.action.IAction;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public class Case implements ITemplate {
  
  private String name;
  
  private List<IAction> actions = new ArrayList<IAction>();
  
  public Case(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public Case addAction(IAction... actions) {
    for (IAction action : actions) {
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
    for (IAction action : actions) {
      sb.append("    ");
      sb.append(action.transform());
      sb.append("\n");
    }
    sb.append("  }\n");
    return sb.toString();
  }

}
