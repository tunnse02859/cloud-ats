/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
@SuppressWarnings("serial")
public class GoBack extends AbstractAction {

  public String transform() throws IOException {
	  StringBuilder sb = new StringBuilder();
		sb.append("try { \n");
		sb.append("     wd.navigate().back();\n");
		sb.append("   } catch (Exception e) { \n");
		sb.append("     SimpleDateFormat dateFormat = new SimpleDateFormat(\"yyyy/MM/dd HH:mm:ss\");\n");
		sb.append("     long time = dateFormat.parse(dateFormat.format(new Date())).getTime();\n");
		sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+time+\"_goBack.png\"));\n");
		sb.append("     throw e ; \n");
		sb.append("   }\n");  
    return  sb.toString();
  }

  public String getAction() {
    return "goBack";
  }

}
