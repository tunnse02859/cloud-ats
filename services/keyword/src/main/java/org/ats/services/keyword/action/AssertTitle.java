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
public class AssertTitle extends AbstractAction{

  private boolean negated;
  
  private Value title;
  
  public AssertTitle(Value title, boolean negated) {
    this.title = title;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append(negated ? "assertNotEquals(" : "assertEquals(");
	sb.append("     wd.getTitle(), ").append(title).append(");\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (AssertionError ae) { \n");
	sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_assertTitle.png\"));\n");
	sb.append("     ae.printStackTrace();\n");
	sb.append("     throw ae ; \n");
	sb.append("   }\n");
    return sb.toString();
  }

  public String getAction() {
    return "assertTitle";
  }

}
