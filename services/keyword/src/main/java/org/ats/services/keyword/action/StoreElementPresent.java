/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.VariableFactory;
import org.ats.services.keyword.VariableFactory.DataType;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
@SuppressWarnings("serial")
public class StoreElementPresent extends AbstractAction {

  private AbstractLocator locator;
  
  private String variable;
  
  private VariableFactory factory;
  
  public StoreElementPresent(AbstractLocator locator, String variable, VariableFactory factory) {
    this.locator = locator;
    this.variable = variable;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.BOOLEAN, variable)).append(" = true;\n");
	sb.append("try { \n");
	sb.append(variable);
	sb.append(" = (wd.findElements(@locator).size() != 0);\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_storeElementPresent.png\"));\n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "storeElementPresent";
  }
}
  