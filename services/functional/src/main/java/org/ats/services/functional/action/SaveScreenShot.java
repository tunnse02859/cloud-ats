/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class SaveScreenShot implements IAction{

  private Value file;
  
  public SaveScreenShot(Value file) {
    this.file = file;
  }
  
  public String transform() throws IOException {
    return null;
  }

  public String getAction() {
    return null;
  }

}
