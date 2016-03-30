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
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("     wd = (FirefoxDriver) wd.switchTo().frame(@identifier);\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_switchToFrame.png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), identifier.transform());
  }

  public String getAction() {
    return "switchToFrame";
  }

}
