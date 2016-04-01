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
public class AssertEval extends  AbstractAction {

  private Value script;
  
  private Value value;
  
  private boolean negated;
  
  public AssertEval(Value script, Value value, boolean negated) {
    this.script = script;
    this.value = value;
    this.negated = negated;
  }
  @Override
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append(negated ? "assertNotEquals(" : "assertEquals(");
	sb.append("     wd.executeScript(@script), @value);\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (AssertionError ae) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_assertEval.png\"));\n");
	sb.append("     ae.printStackTrace();\n");
	sb.append("     throw ae ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), script.transform(), value.transform());
  }

  @Override
  public String getAction() {
    return "assertEval";
  }

}
