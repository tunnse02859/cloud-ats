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
public class AnswerAlert extends AbstractAction {

  private Value text;
  
  public AnswerAlert(Value text) {
    this.text = text;
  }
  
  @Override
  public String transform() throws IOException {
    
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("     wd.switchTo().alert().sendKeys(@text);\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/\"+System.currentTimeMillis()+\".png\"));\n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  @Override
  public String getAction() {
    return "answerAlert";
  }

}
