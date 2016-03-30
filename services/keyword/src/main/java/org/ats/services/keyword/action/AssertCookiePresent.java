/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class AssertCookiePresent extends AbstractAction{

  private Value name;
  
  private boolean negated;
  
  public AssertCookiePresent(Value name, boolean negated) {
    
    this.name = name;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
   	sb.append("try { \n");
   	sb.append(      negated ? "assertFalse(" : "assertTrue(");
   	sb.append("     (wd.manage().getCookieNamed(").append(name).append(") != null));\n");
   	sb.append("     System.out.println(\"[End][Step]\"); \n");
   	sb.append("   } catch (AssertionError ae) { \n");
   	sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
   	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_assertCookiePresent.png\"));\n");
   	sb.append("     ae.printStackTrace();\n");
   	sb.append("     throw ae ; \n");
   	sb.append("   }\n");
    return sb.toString();
  }

  public String getAction() {
    return "assertCookiePresent";
  }

}
