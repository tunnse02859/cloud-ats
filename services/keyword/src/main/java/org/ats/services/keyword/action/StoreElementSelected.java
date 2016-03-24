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
public class StoreElementSelected extends AbstractAction {
  
  private String variable;
  
  private AbstractLocator locator;
  
  private VariableFactory factory;
  
  public StoreElementSelected(String variable, AbstractLocator locator, VariableFactory factory) {
    this.variable = variable;
    this.locator = locator;
    this.factory = factory;
  }

  @Override
  public String transform() throws IOException {
    
//    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.BOOLEAN, variable));
//    sb.append(" = (wd.findElement(@locator).isSelected());\n");
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append(factory.getVariable(DataType.BOOLEAN, variable));
	sb.append(" = (wd.findElement(@locator).isSelected());\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/\"+time+\".png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), locator.transform());
  }

  @Override
  public String getAction() {
    return "storeElementSelected";
  }

}
