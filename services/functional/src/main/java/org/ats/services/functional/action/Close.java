/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 10, 2015
 */
@SuppressWarnings("serial")
public class Close extends AbstractAction {

  public String transform() throws IOException {
    return "wd.close();\n";
  }

  public String getAction() {
    return "close";
  }

}
