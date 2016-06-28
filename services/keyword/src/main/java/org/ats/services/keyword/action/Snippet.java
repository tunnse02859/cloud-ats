package org.ats.services.keyword.action;

import java.io.IOException;

import org.ats.services.keyword.ActionFactory;

public class Snippet extends AbstractAction{
  
  private String code;
  
  public Snippet(String code) {
    this.code = code;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("try { \n");
    
    sb.append(    code);
    
    sb.append("     System.out.println(\"[End][Step]\"); \n");
    sb.append("   } catch (Exception e) { \n");
    sb.append("     wd.getScreenshotAs(FILE).renameTo(new File(\"target/error_\"+S"
        + "ystem.currentTimeMillis()+\"snippet.png\"));\n");
    sb.append("     e.printStackTrace();\n");
    sb.append("     throw e ; \n");
    sb.append("   }\n");
    return sb.toString();
  }

  public String getAction() {
    return "snippet";
  }
  
}
