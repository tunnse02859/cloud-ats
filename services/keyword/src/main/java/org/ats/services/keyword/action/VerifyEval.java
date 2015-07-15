/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.common.MapBuilder;
import org.ats.services.keyword.Value;
import org.rythmengine.RythmEngine;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class VerifyEval extends AbstractAction {

  private Value script;
  
  private Value value;
  
  private boolean negated;
  
  public VerifyEval(Value script, Value value, boolean negated) {
    this.script = script;
    this.value = value;
    this.negated = negated;
  }
  
  @Override
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!").append("wd.executeScript(@script).equals(@value)) {\n");
    sb.append("      System.out.println(\"").append(negated ? "!" : "").append("verifyEval failed\");\n");
    sb.append("    }\n");
    
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), script.transform(), value.transform());
  }

  @Override
  public String getAction() {
    return "verifyEval";
  }

}
