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
	  StringBuilder sb = new StringBuilder();
		sb.append("try {\n");
		sb.append("   wd.get(@url);\n");
		sb.append("   System.out.println(\"[End][Step]\"); \n");
		sb.append("} catch (Exception e) { \n");
		sb.append("   wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_get.png\"));\n");
		sb.append("   e.printStackTrace();\n");
		sb.append("   throw e ; \n");
		sb.append("}\n");
    return Rythm.render(sb.toString(), url.transform());
  }

  public String getAction() {
    return "get";
  }
}
