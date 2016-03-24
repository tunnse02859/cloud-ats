/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.locator.AbstractLocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 10, 2015
 */
@SuppressWarnings("serial")
public class DragToAndDropElement extends AbstractAction {

  /** .*/
  private AbstractLocator source;
  
  /** .*/
  private AbstractLocator destination;
  
  public DragToAndDropElement(AbstractLocator source, AbstractLocator destination) {
    this.source = source;
    this.destination = destination;
  }
  
  public String transform() throws IOException {
    String template = "new Actions(wd).dragAndDrop(wd.findElement(@source), wd.findElement(@destination)).build().perform();\n";
    
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append("     new Actions(wd).dragAndDrop(wd.findElement(@source), wd.findElement(@destination)).build().perform();\n");
	sb.append("   } catch (Exception e) { \n");
	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/\"+time+\".png\"));\n");
	sb.append("     throw e ; \n");
	sb.append("   }\n");
    return Rythm.render(sb.toString(), source.transform(), destination.transform());
  }

  public String getAction() {
    return "dragToAndDropElement";
  }

}
