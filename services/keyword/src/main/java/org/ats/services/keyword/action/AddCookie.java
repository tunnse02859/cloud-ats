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
public class AddCookie extends AbstractAction {

  private Value name, value;
  
  private String option;
  
  public AddCookie(Value name, Value value, String option) {
    this.name = name;
    this.value = value;
    this.option = option;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("wd.manage().addCookie(");
    sb.append("new Cookie.Builder(").append(name).append(", ").append(value).append(")");
    
    if (option != null) {
      for (String s : option.split(",")) {
        String[] foo = s.trim().split("=");
        if (foo.length == 1) continue;
        if (foo[0].equals("path")) {
          sb.append(".path(").append("\"").append(foo[1]).append("\")");
        }
        else if (foo[0].equals("max_age")) {
          sb.append(".expiresOn(new Date(new Date().getTime() + ");
          sb.append(Integer.parseInt(foo[1])).append("000").append("l").append("))");
        }
      }
    }
    sb.append(".build()");
    sb.append(");\n");
    return sb.toString();
  }

  public String getAction() {
    return "addCookie";
  }

}
