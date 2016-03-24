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
 * Apr 9, 2015
 */
@SuppressWarnings("serial")
public class SetElementText extends AbstractAction {

  private AbstractLocator locator;
  
  private Value text;
  
  public SetElementText(AbstractLocator locator, Value text) {
    this.locator = locator;
    this.text = text;
  }
  
  public String transform() throws IOException {
//    String template = "wd.findElement(@locator).click();\n"
//        + "wd.findElement(@locator).clear();\n"
//        + "wd.findElement(@locator).sendKeys(@text);\n";
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("     wd.findElement(@locator).click();\n");
	sb.append("     wd.findElement(@locator).clear();\n");
	sb.append("     wd.findElement(@locator).sendKeys(@text);\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/\"+time+\".png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), locator.transform(), text.transform());
  }

  public String getAction() {
    return "setElementText";
  }
}
