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
 * Apr 13, 2015
 */
@SuppressWarnings("serial")
public class StorePageSource extends AbstractAction {

  private String variable;
  
  private VariableFactory factory;
  
  public StorePageSource(String variable, VariableFactory factory) {
    this.variable = variable;
    this.factory = factory;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.STRING, variable)).append(" = wd.getPageSource();\n");
    sb.append("     System.out.println(\"[End][Step]\"); \n");
    return sb.toString();
  }

  public String getAction() {
    return "storePageSource";
  }

}
