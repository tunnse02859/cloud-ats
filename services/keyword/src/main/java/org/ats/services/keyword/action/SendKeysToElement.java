/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.common.MapBuilder;
import org.ats.services.keyword.Value;
import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.RythmEngine;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 9, 2015
 */
@SuppressWarnings("serial")
public class SendKeysToElement extends AbstractAction {

  private AbstractLocator locator;
  
  private Value text;
  
  public SendKeysToElement(AbstractLocator locator, Value text) {
    this.locator = locator;
    this.text = text;
  }
  
  public String transform() throws IOException {
    String template = "wd.findElement(@locator).click();\n"
        + "    wd.findElement(@locator).sendKeys(@text);\n";
    
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(template, locator.transform(), text.transform());
  }

  public String getAction() {
    return "sendKeysToElement";
  }

}
