/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class SaveScreenShot extends AbstractAction {

  private Value file;
  
  public SaveScreenShot(Value file) {
    this.file = file;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("wd.getScreenshotAs(FILE).renameTo(new File(");
    sb.append(file);
    sb.append("));\n");
    return sb.toString();
  }

  public String getAction() {
    return "saveScreenshot";
  }

}
