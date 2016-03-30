/**
 * 
 */
package org.ats.services.keyword.action;

import java.io.IOException;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
@SuppressWarnings("serial")
public class Pause extends AbstractAction {

  private long waitTime;
  
  public Pause(long waitTime) {
    this.waitTime = waitTime;
  }
  
  public String transform() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("try {\n");
    sb.append(" Thread.sleep(");
    sb.append(waitTime).append("l);\n");
    sb.append("     System.out.println(\"[End][Step]\"); \n");
    sb.append(" } catch (Exception e) {\n ");
    sb.append("     e.printStackTrace();\n");
    sb.append("     throw new RuntimeException(e);\n");
    sb.append("}\n");
    return sb.toString();
  }

  public String getAction() {
    return "pause";
  }

}
