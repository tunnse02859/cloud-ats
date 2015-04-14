/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public class Get implements IAction {
  
  private String url;
  
  public Get(String url) {
    this.url = url;
  }

  public String transform() throws IOException {
    return Rythm.render("wd.get(@url);\n", url);
  }

  public String getAction() {
    return "get";
  }
}
