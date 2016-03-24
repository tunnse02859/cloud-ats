/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.ats.services.keyword.VariableFactory;
import org.ats.services.keyword.VariableFactory.DataType;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
@SuppressWarnings("serial")
public class StoreTextPresent extends AbstractAction {

  private String variable;
  
  private Value text;
  
  private VariableFactory factory;
  
  public StoreTextPresent(String variable, Value text, VariableFactory factory) {
    this.variable = variable;
    this.text = text;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
//    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.BOOLEAN, variable));
//    sb.append(" = wd.findElement(By.tagName(\"html\")).getText().contains(@text);\n");
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append(factory.getVariable(DataType.BOOLEAN, variable));
	sb.append(" = wd.findElement(By.tagName(\"html\")).getText().contains(@text);\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/\"+time+\".png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), text.transform());
  }

  public String getAction() {
    return "storeTextPresent";
  }

}
