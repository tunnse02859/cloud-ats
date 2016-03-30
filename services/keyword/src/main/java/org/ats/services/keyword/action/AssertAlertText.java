/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.rythmengine.Rythm;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
@SuppressWarnings("serial")
public class AssertAlertText extends AbstractAction{

  private Value text;
  
  private boolean negated;
  
  public AssertAlertText(Value text, boolean negated) {
    this.text = text;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
   	sb.append("try { \n");
   	sb.append(      negated ? "assertNotEquals(" : "assertEquals(");
   	sb.append("     wd.switchTo().alert().getText(), ");
   	sb.append(text);
   	sb.append(");\n");
   	sb.append("    System.out.println(\"[End][Step]\"); \n");
   	sb.append("   } catch (AssertionError ae) { \n");
   	sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
   	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/_error\"+time+\"_assertAlertText.png\"));\n");
   	sb.append("     throw ae ; \n");
   	sb.append("   }\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "assertAlertText";
  }

}
