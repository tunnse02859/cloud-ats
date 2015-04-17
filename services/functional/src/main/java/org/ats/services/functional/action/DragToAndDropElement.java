/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.locator.ILocator;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 10, 2015
 */
public class DragToAndDropElement implements IAction {

  /** .*/
  private ILocator source;
  
  /** .*/
  private ILocator destination;
  
  public DragToAndDropElement(ILocator source, ILocator destination) {
    this.source = source;
    this.destination = destination;
  }
  
  public String transform() throws IOException {
    String template = "new Actions(wd).dragAndDrop(wd.findElement(@source), wd.findElement(@destination).build().perform();\n";
    return Rythm.render(template, source.transform(), destination.transform());
  }

  public String getAction() {
    return "dragToAndDropElement";
  }

}
