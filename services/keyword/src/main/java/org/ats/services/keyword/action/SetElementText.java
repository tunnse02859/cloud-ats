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
    StringBuilder sb = new StringBuilder();
    
    sb.append("try { \n");
    
    if (text.isVariable()) {
      sb.append("     if (!(\"__blank__\".equals(@text))) {\n");
      sb.append("     wd.findElement(@locator).click();\n");
      sb.append("     wd.findElement(@locator).clear();\n");
      sb.append("     wd.findElement(@locator).sendKeys(@text);\n");
      sb.append("     System.out.println(\"[End][Step]\");\n");
      sb.append("     } else {\n");
      sb.append("     System.out.println(\"[End][Step]\");\n");
      sb.append("     }\n");
    } else {
      sb.append("     wd.findElement(@locator).click();\n");
      sb.append("     wd.findElement(@locator).clear();\n");
      sb.append("     wd.findElement(@locator).sendKeys(@text);\n");
      sb.append("     System.out.println(\"[End][Step]\");\n");
    }
    
    sb.append("   } catch (Exception e) { \n");
    sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_setElementText.png\"));\n");
    sb.append("     e.printStackTrace();\n");
    sb.append("     throw e ; \n");
	
	sb.append("   }\n");
    return Rythm.render(sb.toString(), text.transform(), locator.transform());
  }

  public String getAction() {
    return "setElementText";
  }
}
