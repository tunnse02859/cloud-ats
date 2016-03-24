/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class VerifyAlertPresent extends AbstractAction {
  
  private boolean negated;

  public VerifyAlertPresent(boolean negated) {
    this.negated = negated;
  }
  
  @Override
  public String transform() throws IOException {
    
//    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!").append("isAlertPresent(wd)) {\n");
//    sb.append("      System.out.println(\"").append(negated ? "!" : "").append("verifyAlertPresent failed\");\n    }\n");
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("     if (").append(negated ? "" : "!").append("isAlertPresent(wd)) {\n");
	sb.append("     System.out.println(\"").append(negated ? "!" : "").append("verifyAlertPresent failed\");\n    }\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/\"+time+\".png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return sb.toString();
  }

  @Override
  public String getAction() {
    return "verifyAlertPresent";
  }

}
