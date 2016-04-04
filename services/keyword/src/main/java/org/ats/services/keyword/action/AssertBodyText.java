/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
@SuppressWarnings("serial")
public class AssertBodyText extends AbstractAction {

  private Value text;
  
  private boolean negated;
  
  public AssertBodyText(Value text, boolean negated) {
    this.text = text;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
   	sb.append("try { \n");
   	sb.append(      negated ? "assertNotEquals(" : "assertEquals(");
   	sb.append("     wd.findElement(By.tagName(\"html\")).getText(), @text);\n");
   	sb.append("     System.out.println(\"[End][Step]\"); \n");
   	sb.append("   } catch (AssertionError ae) { \n");
   	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_assertBodyText.png\"));\n");
   	sb.append("     ae.printStackTrace();\n");
   	sb.append("     throw ae ; \n");
   	sb.append("   }\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "assertBodyText";
  }

}
