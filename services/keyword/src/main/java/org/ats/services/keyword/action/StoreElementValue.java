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
 * Apr 16, 2015
 */
@SuppressWarnings("serial")
public class StoreElementValue extends AbstractAction {
  
  private AbstractLocator locator;
  
  private String variable;
  
  private VariableFactory factory;

  public StoreElementValue(AbstractLocator locator, String variable, VariableFactory factory) {
    this.locator = locator;
    this.variable = variable;
    this.factory = factory;
  }
  
  @Override
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.STRING, variable)).append(" = \"\";\n");
	sb.append("try { \n");
	sb.append(variable).append(" = wd.findElement(@locator).getAttribute(");
	sb.append("\"value\")");
    sb.append(";\n");
    sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_storeElementValue.png\"));\n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  @Override
  public String getAction() {
    return "storeElementValue";
  }

}
