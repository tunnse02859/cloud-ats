/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class AssertCookieByName extends AbstractAction{

  private Value name, value;
  
  private boolean negated;
  
  public AssertCookieByName(Value name, Value value, boolean negated) {
    this.name = name;
    this.value = value;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.manage().getCookieNamed(").append(name).append(").getValue(), @value);\n");
    
    return Rythm.render(sb.toString(), value.transform());
  }

  public String getAction() {
    return "assertCookieByName";
  }

}
