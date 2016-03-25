/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
@SuppressWarnings("serial")
public class AssertElementStyle extends AbstractAction{

  private Value propertyName, value;
  
  private AbstractLocator locator;
  
  private boolean negated;
  
  public AssertElementStyle(Value propertyName,Value value, AbstractLocator locator, boolean negated) {
    this.propertyName = propertyName;
    this.value = value;
    this.locator = locator;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append(      negated ? "assertNotEquals(" : "assertEquals(");
	sb.append("     wd.findElement(@locator).getCssValue(");
    sb.append(propertyName);
    sb.append("), ");
    sb.append(value);
    sb.append(");\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_assertElementStyle.png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), locator.transform(),value.transform(),propertyName.transform());
  }

  public String getAction() {
    return "assertElementStyle";
  }

}
