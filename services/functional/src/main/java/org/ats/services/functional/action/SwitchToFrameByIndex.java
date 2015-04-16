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
public class SwitchToFrameByIndex implements IAction {

  private Value index;

  /**
   * 
   */
  SwitchToFrameByIndex(Value index) {
    this.index = index;
  }
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("wd = (FirefoxDriver) wd.switchTo().frame(@index);\n");
    return Rythm.render(sb.toString(), index.toString());
  }

  public String getAction() {
    return "testSwitchToFrameByIndex";
  }
  
}
