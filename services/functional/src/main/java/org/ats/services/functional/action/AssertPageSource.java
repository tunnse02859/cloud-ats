/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.functional.Value;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 13, 2015
 */
@SuppressWarnings("serial")
public class AssertPageSource extends AbstractAction {

  private Value source;
  
  private boolean negated;
  
  public AssertPageSource(Value source, boolean negated) {
    this.source = source;
    this.negated = negated;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
    sb.append("wd.getPageSource(), ").append(source.transform()).append(");\n");
    return sb.toString();
  }

  public String getAction() {
    return "assertPageSource";
  }

}
