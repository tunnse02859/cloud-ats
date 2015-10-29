/**
 * 
 */
package org.ats.common;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 14, 2014
 */
public class StringUtil {

  public static String readStream(InputStream is) throws IOException {
    BufferedInputStream bis = new BufferedInputStream(is);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buff = new byte[1024];
    for (int l = bis.read(buff); l != -1; l = bis.read(buff)) {
      baos.write(buff, 0, l);
    }
    return new String(baos.toByteArray(), "UTF-8");
  }
  
  public static String normalizeName(String name) {
    char[] chars = name.toCharArray();
    StringBuilder sb = new StringBuilder();
    int i = 0;
    
    for(i = 0; i < chars.length; i++) {
      if(Character.isJavaIdentifierStart(chars[i])) {
        sb.append(chars[i]);
        break;
      }
    }
    for(int j = i+1; j < chars.length; j++) {
      if (Character.isJavaIdentifierPart(chars[j])) sb.append(chars[j]);
    }
    return sb.toString();
  }
}
