/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
public class VerifyPageSource implements IAction {
  
  private Value source;
  
  private boolean negated;
  
  public VerifyPageSource(Value source, boolean negated) {
   this.source = source;
   this.negated = negated;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!");
    sb.append("wd.getPageSource().equals(@source)) {\n");
    sb.append("System.out.println(\"").append(negated ? "!" : "").append("verifyPageSource failed\");\n");
    sb.append("}\n");
    return Rythm.render(sb.toString(), source.transform());
  }

  public String getAction() {
    return "verifyPageSource";
  }

}
