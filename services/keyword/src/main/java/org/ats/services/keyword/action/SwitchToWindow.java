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
public class SwitchToWindow extends AbstractAction {

  private Value name;

  public SwitchToWindow(Value name) {
    this.name = name;
  }
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("     wd = (FirefoxDriver) wd.switchTo().window(@name);\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_switchToWindow.png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), name.toString());
  }

  public String getAction() {
    return "switchToWindow";
  }
  
}
