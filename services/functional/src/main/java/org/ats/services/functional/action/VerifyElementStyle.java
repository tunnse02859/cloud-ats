/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.common.MapBuilder;
import org.ats.services.functional.Value;
import org.ats.services.functional.locator.ILocator;
import org.rythmengine.RythmEngine;

/**
 * @author NamBV2
 *
 * Apr 17, 2015
 */
public class VerifyElementStyle implements IAction{

  private Value propertyName, value;
  
  private ILocator locator;
  
  private boolean negated;
  
  public VerifyElementStyle(Value propertyName,Value value, ILocator locator, boolean negated) {
    this.propertyName = propertyName;
    this.value = value;
    this.locator = locator;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "if (wd." : "if (!wd.");
    sb.append("findElement(@locator).getCssValue(");
    sb.append(propertyName);
    sb.append(").equals(");
    sb.append(value);
    sb.append(")) {\n      System.out.println(\"").append(negated ? "!" : "").append("verifyElementStyle failed\");\n    }\n");
    
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), locator.transform(), propertyName.transform(), value.transform());
  }

  @Override
  public String getAction() {
    return "verifyElementStyle";
  }

}
