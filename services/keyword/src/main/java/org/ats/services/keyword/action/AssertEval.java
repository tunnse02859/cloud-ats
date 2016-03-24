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
//    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
//    sb.append("wd.executeScript(@script), @value);\n");
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append(negated ? "assertNotEquals(" : "assertEquals(");
	sb.append("     wd.executeScript(@script), @value);\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/\"+time+\".png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), script.transform(), value.transform());
  }

  @Override
  public String getAction() {
    return "assertEval";
  }

}
