/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.VariableFactory;
import org.ats.services.keyword.VariableFactory.DataType;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 16, 2015
 */
@SuppressWarnings("serial")
public class StoreTitle extends AbstractAction {
  
  private String variable;
  
  private VariableFactory factory;

  public StoreTitle(String variable, VariableFactory factory) {
    this.variable = variable;
    this.factory = factory;
  }
  
  @Override
  public String transform() throws IOException {
	  StringBuilder sb = new StringBuilder();
		sb.append("try { \n");
		sb.append(factory.getVariable(DataType.STRING, variable) + " = wd.getTitle();\n");
		sb.append("   } catch (Exception e) { \n");
		sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
		sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
		sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/\"+time+\".png\"));\n");
		sb.append("     throw e ; \n");
		sb.append("   }\n");
    return sb.toString();
  }

  @Override
  public String getAction() {
    return "storeTitle";
  }

}
