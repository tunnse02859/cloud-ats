/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class DismissAlert extends AbstractAction {

  @Override
  public String transform() throws IOException {
    return "wd.switchTo().alert().dismiss();\n";
  }

  @Override
  public String getAction() {
    return "dismissAlert";
  }

}
