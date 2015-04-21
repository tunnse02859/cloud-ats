/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;
import org.ats.services.functional.VariableFactory;
import org.ats.services.functional.VariableFactory.DataType;
import org.rythmengine.Rythm;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class StoreEval implements IAction {

  private Value script;
  
  private String variable;
  
  private VariableFactory factory;
  
  public StoreEval(Value script, String variable, VariableFactory factory) {
    this.script = script;
    this.variable = variable;
    this.factory = factory;
  }
  
  @Override
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(factory.getVariable(DataType.OBJECT, variable));
    sb.append(" = wd.executeScript(@script);\n");
    return Rythm.render(sb.toString(), script.transform());
  }

  @Override
  public String getAction() {
    return "storeEval";
  }

}
