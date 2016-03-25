/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.common.MapBuilder;
import org.ats.services.keyword.Value;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.RythmEngine;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn@SuppressWarnings("serial")

 */
@SuppressWarnings("serial")
public class VerifyElementValue extends AbstractAction {

  private AbstractLocator locator;
  
  private Value value;
  
  private boolean negated;

  public VerifyElementValue(AbstractLocator locator, Value value, boolean negated) {
    this.locator = locator;
    this.value = value;
    this.negated = negated;
    
  }
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("     if (").append(negated ? "" : "!");
	sb.append("wd.findElement(@locator).getAttribute(\"value\").equals(@value)) {\n");
    sb.append("      System.out.println(\"").append(negated ? "!" : "").append("verifyElementValue failed\");\n");
    sb.append("    }\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_verifyElementValue.png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), locator.transform(), value.transform());
  }

  public String getAction() {
    return "verifyElementValue";
  }
  
}
