/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
@SuppressWarnings("serial")
public class AssertAlertPresent extends AbstractAction {

  private boolean negated;
  
  public AssertAlertPresent(boolean negated) {
    this.negated = negated;
  }
  
  public String transform() throws IOException {
//    StringBuilder sb = new StringBuilder(negated ? "assertFalse(" : "assertTrue(");
//    sb.append("isAlertPresent(wd));\n");
    StringBuilder sb = new StringBuilder();
   	sb.append("try { \n");
   	sb.append(      negated ? "assertFalse(" : "assertTrue(");
   	sb.append("     isAlertPresent(wd));\n");
   	sb.append("   } catch (Exception e) { \n");
   	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
   	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
   	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/\"+time+\".png\"));\n");
   	sb.append("     throw e ; \n");
   	sb.append("   }\n");
    return sb.toString();
  }

  public String getAction() {
    return "assertAlertPresent";
  }

}
