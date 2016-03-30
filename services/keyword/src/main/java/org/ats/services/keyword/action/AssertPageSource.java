/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.Value;

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
    StringBuilder sb = new StringBuilder();
	sb.append("try { \n");
	sb.append(     negated ? "assertNotEquals(" : "assertEquals(");
	sb.append("     wd.getPageSource(), ").append(source.transform()).append(");\n");
	sb.append("     System.out.println(\"[End][Step]\"); \n");
	sb.append("   } catch (AssertionError ae) { \n");
	sb.append("     time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_assertPageSource.png\"));\n");
	sb.append("     ae.printStackTrace();\n");
	sb.append("     throw ae ; \n");
	sb.append("   }\n");
    return sb.toString();
  }

  public String getAction() {
    return "assertPageSource";
  }

}
