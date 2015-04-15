/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 10, 2015
 */
public class DeleteCookie implements IAction {

  private Value name;
  
  public DeleteCookie(Value name) {
    this.name = name;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("if (wd.manage().getCookieNamed(").append(name.transform()).append(") != null) {");
    sb.append(" wd.manage().deleteCookie(wd.manage().getCookieNamed(").append(name.transform()).append("");
    sb.append(")); }\n");
    return sb.toString();
  }

  public String getAction() {
    return "deleteCookie";
  }

}
