/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
@SuppressWarnings("serial")
public class AssertText extends AbstractAction {
  
  private AbstractLocator locator;
  
  private Value text;
  
  private boolean negated;
  
  public AssertText(AbstractLocator locator, Value text, boolean negated) {
    this.locator = locator;
    this.text = text;
    this.negated = negated;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.findElement(@locator).getText(), @text);\n");
    return Rythm.render(sb.toString(), locator.transform(), text.transform());
  }

  public String getAction() {
    return "assertText";
  }

}
