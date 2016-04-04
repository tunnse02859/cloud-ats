/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 10, 2015
 */
@SuppressWarnings("serial")
public class DeleteCookie extends AbstractAction {

  private Value name;
  
  public DeleteCookie(Value name) {
    this.name = name;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("try { \n");
    sb.append("if (wd.manage().getCookieNamed(").append(name.transform()).append(") != null) {\n");
    sb.append("      wd.manage().deleteCookie(wd.manage().getCookieNamed(").append(name.transform()).append("));\n");
    sb.append("    }\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return sb.toString();
  }

  public String getAction() {
    return "deleteCookie";
  }

}
