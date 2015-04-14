/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public class GoBack implements IAction {

  public String transform() throws IOException {
    return  "wd.navigate().back();\n";
  }

  public String getAction() {
    return "goBack";
  }

}
