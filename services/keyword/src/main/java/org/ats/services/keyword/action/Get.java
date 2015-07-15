/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;
import org.rythmengine.Rythm;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class Get extends AbstractAction {
  
  private Value url;
  
  public Get(Value url) {
    this.url = url;
  }

  public String transform() throws IOException {
    return Rythm.render("wd.get(@url);\n", url.transform());
  }

  public String getAction() {
    return "get";
  }
}
