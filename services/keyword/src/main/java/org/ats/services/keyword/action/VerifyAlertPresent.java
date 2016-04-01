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
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("     if (").append(negated ? "!" : "").append("isAlertPresent(wd)) {\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("    } else {\n");
    sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_verifyAlertPresent.png\"));\n");
    sb.append("    }\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_verifyAlertPresent.png\"));\n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return sb.toString();
  }

  @Override
  public String getAction() {
    return "verifyAlertPresent";
  }

}
