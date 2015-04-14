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
 * Apr 10, 2015
 */
public class AssertTextPresent implements IAction {

  private Value text;
  
  private boolean negated;
  
  public AssertTextPresent(Value text, boolean negated) {
    this.text = text;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(negated ? "assertFalse(" : "assertTrue(");
    sb.append("wd.findElement(By.tagName(\"html\")).getText().contains(@text)");
    sb.append(")\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "assertTextPresent";
  }

}
