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
public class VerifyCookiePresent extends AbstractAction {

  private Value name;

  private boolean negated;
  
  public VerifyCookiePresent(Value name, boolean negated) {
    this.name = name;
    this.negated = negated;
  }

  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!");
    sb.append("(wd.manage().getCookieNamed(@name) != null)) {\n");
    sb.append("      System.out.println(\"").append(negated ? "!" : "").append("verifyCookiePresent failed\");\n");
    sb.append("    }\n");
    
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), name.transform());
  }

  public String getAction() {
    return "verifyCookiePresent";
  }
}
