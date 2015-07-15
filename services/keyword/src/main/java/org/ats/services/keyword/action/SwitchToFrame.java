/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class SwitchToFrame extends AbstractAction {

  private Value identifier;
  
  public SwitchToFrame(Value identifier) {
    this.identifier = identifier;
  }
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("wd = (FirefoxDriver) wd.switchTo().frame(@identifier);\n");
    return Rythm.render(sb.toString(), identifier.transform());
  }

  public String getAction() {
    return "switchToFrame";
  }

}
