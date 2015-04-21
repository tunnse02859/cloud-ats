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
public class AnswerAlert implements IAction {

  private Value text;
  
  /**
   * 
   */
  public AnswerAlert(Value text) {
    this.text = text;
    
  }
  @Override
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder("wd.switchTo().alert().sendKeys(@text);\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  @Override
  public String getAction() {
    return "answerAlert";
  }

}
