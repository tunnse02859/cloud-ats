/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 14, 2015
 */
@SuppressWarnings("serial")
public class AssertCurrentUrl extends AbstractAction {
  
  private Value url;
  
  private boolean negated;
  
  public AssertCurrentUrl(Value url, boolean negated) {
    this.url = url;
    this.negated = negated;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
   	sb.append("try { \n");
   	sb.append(      negated ? "assertNotEquals(" : "assertEquals(");
   	sb.append("     wd.getCurrentUrl(), ").append(url.transform()).append(");\n");
   	sb.append("     System.out.println(\"[End][Step]\"); \n");
   	sb.append("   } catch (AssertionError ae) { \n");
   	sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
   	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_assertCurrentUrl.png\"));\n");
   	sb.append("     throw ae ; \n");
   	sb.append("   }\n");
    return sb.toString();
  }

  public String getAction() {
    return "assertCurrentUrl";
  }

}
