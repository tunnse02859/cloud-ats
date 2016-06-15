/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;
import java.util.List;

import org.ats.services.keyword.ActionFactory;
import org.ats.services.keyword.Case;
import org.rythmengine.Rythm;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */


@SuppressWarnings("serial")
public class Loopor extends AbstractAction {
  
  private ActionFactory actionFactory;
  
  private int times;
  
  private List<String> variables;
  
  private List<JsonNode> actions;
  
  public Loopor(int times, List<String> variables, List<JsonNode> actions, ActionFactory actionFactory) {
    this.times = times;
    this.variables = variables;
    this.actions = actions;
    this.actionFactory = actionFactory;
  }

  public String transform() throws IOException {
	  StringBuilder sb = new StringBuilder();
	  
	  sb.append("try { \n");
	  
	  for(String param : variables) {
	    sb.append("String[] array_").append(param).append(" = ").append(param).append(".split(\"\\\\|\\\\|\");").append("\n");
	  }
	  
	  sb.append("for(int i = 0; i < @times; i++){ \n");
	  for(String param : variables) {
	    sb.append(param).append(" = ").append("array_").append(param).append("[i];\n");
	  }
	  String rendered = Rythm.render(sb.toString(), times);
	  sb.setLength(0);
	  sb.append(rendered);
	  
	  for (JsonNode json : actions) {
	   AbstractAction action = actionFactory.createAction(json);
	   sb.append(Case.buildStartStepLog(json));
	   sb.append(action.transform());
    }
	  sb.append("}\n");
		sb.append("   } catch (Exception e) { \n");
		sb.append("e.printStackTrace();\n");
		sb.append("throw e ; \n");
		sb.append(" }\n");
    return sb.toString();
  }

  public String getAction() {
    return "loopor";
  }

}
