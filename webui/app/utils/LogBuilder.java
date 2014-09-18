/**
 * 
 */
package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 18, 2014
 */
public class LogBuilder {

  private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
  
  public static void log(StringBuilder sb, String msg) {
    sb.append("[").append(formatter.format(new Date(System.currentTimeMillis()))).append("] ").append(msg).append("\n");
  }
}
