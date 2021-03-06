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
		sb.append("     System.out.println(\"[End][Step]\"); \n");
		sb.append("   } catch (Exception e) { \n");
		sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+System.currentTimeMillis()+\"_goBack.png\"));\n");
		sb.append("     e.printStackTrace();\n");
		sb.append("     throw e ; \n");
		sb.append("   }\n");  
    return  sb.toString();
  }

  public String getAction() {
    return "goBack";
  }

}
