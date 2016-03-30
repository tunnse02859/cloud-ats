/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.VariableFactory;
import org.ats.services.keyword.VariableFactory.DataType;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class StoreAlertPresent extends AbstractAction {

  /** .*/
  private String variable;
  
  /** .*/
  private VariableFactory factory;
  
  public StoreAlertPresent(String variable, VariableFactory factory) {
    this.variable = variable;
    this.factory = factory;
  }
  @Override
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.BOOLEAN, variable)).append(" = true;\n");
	sb.append("try { \n");
	sb.append(variable).append(" = ");
	sb.append("     isAlertPresent(wd);\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_storeAlertPresent.png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return sb.toString();
  }

  @Override
  public String getAction() {
    return "storeAlertPresent";
  }

}
