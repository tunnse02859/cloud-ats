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
//    StringBuilder sb = new StringBuilder(negated ? "assertNotEquals(" : "assertEquals(");
//    sb.append("wd.getCurrentUrl(), ").append(url.transform()).append(");\n");
    StringBuilder sb = new StringBuilder();
   	sb.append("try { \n");
   	sb.append(      negated ? "assertNotEquals(" : "assertEquals(");
   	sb.append("     wd.getCurrentUrl(), ").append(url.transform()).append(");\n");
   	sb.append("   } catch (Exception e) { \n");
   	sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
   	sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
   	sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/\"+time+\".png\"));\n");
   	sb.append("     throw e ; \n");
   	sb.append("   }\n");
    return sb.toString();
  }

  public String getAction() {
    return "assertCurrentUrl";
  }

}
