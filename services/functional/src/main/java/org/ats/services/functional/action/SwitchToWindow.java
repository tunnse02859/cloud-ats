/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class SwitchToWindow implements IAction {

  private Value name;

  /**
   * 
   */
  public SwitchToWindow(Value name) {
    this.name = name;
  }
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("wd = (FirefoxDriver) wd.switchTo().window(@name);\n");
    return Rythm.render(sb.toString(), name.toString());
  }

  public String getAction() {
    return "testSwitchToWindow";
  }
  
}
