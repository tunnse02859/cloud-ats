/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 14, 2015
 */
@SuppressWarnings("serial")
public class AssertCurrentUrl extends AbstractAction {
  
  private Value url;
  
  private boolean negated;
  
  public AssertCurrentUrl(Value url, boolean negated) {
    this.url = url;
    this.negated = negated;
  }

  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.getCurrentUrl(), ").append(url.transform()).append(");\n");
    return sb.toString();
  }

  public String getAction() {
    return "assertCurrentUrl";
  }

}
