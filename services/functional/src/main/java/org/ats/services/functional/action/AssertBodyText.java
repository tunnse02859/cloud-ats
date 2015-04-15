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
public class AssertBodyText implements IAction {

  private Value text;
  
  private boolean neagated;
  
  public AssertBodyText(Value text, boolean negated) {
    this.text = text;
    this.neagated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(neagated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.findElement(By.tagName(\"html\")).getText(), @text);\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "assertBodyText";
  }

}
