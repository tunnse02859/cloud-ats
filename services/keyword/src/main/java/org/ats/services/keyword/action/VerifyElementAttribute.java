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
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class VerifyElementAttribute extends AbstractAction {

  private AbstractLocator locator;

  private Value value;
  
  private Value attributeName;
  
  private boolean negated;
  
  public VerifyElementAttribute(AbstractLocator locator, Value attributeName, Value value, boolean negated) {
    this.locator = locator;
    this.attributeName = attributeName;
    this.value = value;
    this.negated = negated;
    
  }
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("     if (").append(negated ? "!" : "");
	sb.append("     wd.findElement(@locator).getAttribute(@name).equals(@value)) {\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("    } else {\n");
    sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_verifyElementAttribute.png\"));\n");
    sb.append("    }\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_verifyElementAttribute.png\"));\n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(),locator.transform(), attributeName.transform(), value.transform());
  }

  public String getAction() {
    return "verifyElementAttribute";
  }
}
