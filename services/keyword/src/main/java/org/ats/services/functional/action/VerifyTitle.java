/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.common.MapBuilder;
import org.ats.services.functional.Value;
import org.rythmengine.RythmEngine;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class VerifyTitle extends AbstractAction {

  private Value title;
  
  private boolean negated;
  
  public VerifyTitle(Value title, boolean negated) {
    this.title = title;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!");
    sb.append("wd.getTitle().equals(@title)) {\n");
    sb.append("      System.out.println(\"").append(negated ? "!" : "").append("verifyTitle failed\");\n");
    sb.append("    }\n");
    
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), title.transform());
  }

  public String getAction() {
    return "verifyTitle";
  }

}
