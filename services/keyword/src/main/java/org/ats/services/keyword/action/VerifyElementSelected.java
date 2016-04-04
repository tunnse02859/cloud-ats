/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.common.MapBuilder;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.RythmEngine;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class VerifyElementSelected extends AbstractAction {

  private AbstractLocator locator;
  
  private boolean negated;
  
  public VerifyElementSelected(AbstractLocator locator, boolean negated) {
    this.locator = locator;
    this.negated = negated;
  }
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("     if (").append(negated ? "!" : "");
	sb.append("(wd.findElement(@locator).isSelected())) {\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("    } else {\n");
    sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_verifyElementSelected.png\"));\n");
    sb.append("    }\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_verifyElementSelected.png\"));\n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "verifyElementSelected";
  }

}
