/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.common.MapBuilder;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.RythmEngine;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
@SuppressWarnings("serial")
public class VerifyElementPresent extends AbstractAction {
  
  private AbstractLocator locator;
  
  private boolean negated;
  
  public VerifyElementPresent(AbstractLocator locator, boolean negated) {
    this.locator = locator;
    this.negated = negated;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder("if (").append(negated ? "" : "!");
    sb.append("(wd.findElements(@locator).size() != 0)) {\n");
    sb.append("      System.out.println(\"").append(negated ? "!" : "").append("verifyElementPresent failed\");\n");
    sb.append("    }\n");
    
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), locator.transform());
  }

  public String getAction() {
    return "verifyElementPresent";
  }

}
