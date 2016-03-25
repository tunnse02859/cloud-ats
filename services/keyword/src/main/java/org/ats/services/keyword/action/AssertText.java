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
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append(negated ? "assertNotEquals(" : "assertEquals(");
	sb.append("     wd.findElement(@locator).getText(), @text);\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_assertText.png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), locator.transform(), text.transform());
  }

  public String getAction() {
    return "assertText";
  }

}
