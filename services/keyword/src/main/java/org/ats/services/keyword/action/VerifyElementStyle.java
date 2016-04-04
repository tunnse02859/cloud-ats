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
 * @author NamBV2
 *
 * Apr 17, 2015
 */
@SuppressWarnings("serial")
public class VerifyElementStyle extends AbstractAction{

  private Value propertyName, value;
  
  private AbstractLocator locator;
  
  private boolean negated;
  
  public VerifyElementStyle(Value propertyName,Value value, AbstractLocator locator, boolean negated) {
    this.propertyName = propertyName;
    this.value = value;
    this.locator = locator;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("    System.out.println(\"Actual CSSValue : \" + wd.findElement(@locator).getCssValue(");
	sb.append(negated ? "if (!wd." : "if (wd.");
	sb.append("findElement(@locator).getCssValue(");
    sb.append(propertyName);
    sb.append(").equals(");
    sb.append(value);
    sb.append("))  {\n      ");
    sb.append(propertyName);
    sb.append(")");
    sb.append("    System.out.println(\"[End][Step]\"); \n");
    sb.append("    } else {\n");
    sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error\"+System.currentTimeMillis()+\"_verifyElementStyle.png\"));\n");
    sb.append("    }\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_verifyElementStyle.png\"));\n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), locator.transform(), propertyName.transform(), value.transform());
  }

  @Override
  public String getAction() {
    return "verifyElementStyle";
  }

}
