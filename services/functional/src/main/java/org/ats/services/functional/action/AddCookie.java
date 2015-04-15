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
public class AddCookie implements IAction {

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
    //sb.append("new Cookie.Builder(").append("\"").append(name).append("\",\"").append(value).append("\")");
    if (option != null) {
      for (String s : option.split(",")) {
        String[] foo = s.trim().split("=");
        if (foo.length == 1) continue;
        if (foo[0].equals("path")) {
          sb.append(".path(").append("\"").append(foo[1]).append("\")");
        }
        else if (foo[0].equals("max_age")) {
          System.out.println(foo[1]);
          sb.append(".expiresOn(new Date(new Date().getTime() + ");
          sb.append(foo[1] + "000").append("l").append("))");
          // sb.append(Integer.parseInt(foo[1]) * 1000).append("l))");
        }
      }
    }
    sb.append(".build()");
    sb.append(");\n");
    //sb.append(".build();\n");
    //sb.append(");\n");
    return sb.toString();
  }

  public String getAction() {
    return "addCookie";
  }

}
