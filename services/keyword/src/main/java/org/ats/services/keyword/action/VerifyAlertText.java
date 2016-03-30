/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.common.MapBuilder;
import org.ats.services.keyword.Value;
import org.rythmengine.RythmEngine;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
@SuppressWarnings("serial")
public class VerifyAlertText extends AbstractAction {

  private Value text;
  
  private boolean negated;
  
  public VerifyAlertText(Value text, boolean negated) {
    
    this.text = text;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("try { \n");
	sb.append("    System.out.println(\"Actual Text Alert : \"+wd.switchTo().alert().getText()); \n");
	sb.append(      negated ? "if (!wd." : "if (wd.");
	sb.append("     switchTo().alert().getText().equals(");
	sb.append(text);
    sb.append(")) {\n");
    sb.append("    System.out.println(\"[End][Step]\"); \n");
    sb.append("    } else {\n");
    sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_verifyAlertText.png\"));\n");
    sb.append("    }\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_verifyAlertText.png\"));\n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "verifyAlertText";
  }

}
