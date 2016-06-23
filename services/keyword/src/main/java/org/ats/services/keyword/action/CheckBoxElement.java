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
public class CheckBoxElement extends AbstractAction {

  private AbstractLocator locator;
  
  private Value check;
  
  public CheckBoxElement(AbstractLocator locator, Value check) {
    this.locator = locator;
    this.check = check;
  }
  
  public String transform() throws IOException {
	StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("if(\"true\".equals(@check)){");
	sb.append("     wd.findElement(@locator).click();\n");
	sb.append("    }\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_checkBoxElement.png\"));\n");
	sb.append("     e.printStackTrace();\n");
	
	if (check.isVariable()) {
    sb.append("     if (\"__blank__\".equals(@check)) {\n");
    sb.append("     System.out.println(\"[End][Step]\");\n");
    sb.append("     } else throw e;\n");
  } else {
    sb.append("     throw e ; \n");
  }
	
	sb.append("   }\n");
    RythmEngine engine = new RythmEngine(new MapBuilder<String, Boolean>("codegen.compact", false).build());
    return engine.render(sb.toString(), check.transform(), locator.transform());
  }

  public String getAction() {
    return "checkBoxElement";
  }

}
