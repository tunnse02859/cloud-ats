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
@SuppressWarnings("serial")
public class AssertBodyText extends AbstractAction {

  private Value text;
  
  private boolean negated;
  
  public AssertBodyText(Value text, boolean negated) {
    this.text = text;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.findElement(By.tagName(\"html\")).getText(), @text);\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "assertBodyText";
  }

}
