/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class AssertElementAttribute extends AbstractAction{

  private Value attributeName,value;
  
  private AbstractLocator locator;
  
  private boolean negated;
  
  public AssertElementAttribute(Value attributeName, Value value, AbstractLocator locator, boolean negated) {
    this.attributeName = attributeName;
    this.value = value;
    this.locator = locator;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
   	sb.append("try { \n");
   	sb.append(      negated ? "assertNotEquals(" : "assertEquals(");
   	sb.append("     wd.findElement(@locator).getAttribute(").append(attributeName).append("), @value);\n");
   	sb.append("     System.out.println(\"[End][Step]\"); \n");
   	sb.append("   } catch (AssertionError ae) { \n");
   	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_assertElementAttribute.png\"));\n");
   	sb.append("     ae.printStackTrace();\n");
   	sb.append("     throw ae ; \n");
   	sb.append("   }\n");
    return Rythm.render(sb.toString(),locator.transform(),value.transform());
  }

  public String getAction() {
    return "assertElementAttribute";
  }

}
