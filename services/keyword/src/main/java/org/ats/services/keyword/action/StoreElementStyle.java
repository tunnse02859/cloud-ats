/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.ats.services.keyword.VariableFactory;
import org.ats.services.keyword.VariableFactory.DataType;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
@SuppressWarnings("serial")
public class StoreElementStyle extends AbstractAction{

  private Value propertyName;
  
  private String variable;
  
  private AbstractLocator locator;
  
  private VariableFactory factory;
  
  public StoreElementStyle(Value propertyName,String variable, AbstractLocator locator, VariableFactory factory) {
    this.propertyName = propertyName;
    this.variable = variable;
    this.locator = locator;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.STRING, variable)).append(" = \"\";\n");
	sb.append("try { \n");
	sb.append(variable);
	sb.append(" = wd.findElement(@locator).getCssValue(");
    sb.append(propertyName);
    sb.append(");\n");;
    sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_storeElementStyle.png\"));\n");
	sb.append("     e.printStackTrace();\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), locator.transform(),propertyName.transform());
  }

  public String getAction() {
    return "storeElementStyle";
  }

}
