/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.common.MapBuilder;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.RythmEngine;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
@SuppressWarnings("serial")
public class VerifyElementPresent extends AbstractAction {
  
  private AbstractLocator locator;
  
  private boolean negated;
  
  public VerifyElementPresent(AbstractLocator locator, boolean negated) {
    this.locator = locator;
    this.negated = negated;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("try { \n");
	sb.append("     if (").append(negated ? "!" : "");
	sb.append("     wd.findElements(@locator).size() != 0) {\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("    } else {\n");
    sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_verifyElementPresent.png\"));\n");
    sb.append("    }\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error\"+time+\"_verifyElementPresent.png\"));\n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "verifyElementPresent";
  }

}
