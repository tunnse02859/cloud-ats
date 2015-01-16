package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util { 
 
 /**
  * 
  * @param inputFile upload file
  * @param filename
  * @param dir dir contain save file (note have "/" at end dir)
  * @return
  */
  public static boolean uploadFile(File inputFile, String filename, String dir) {

    InputStream inStream = null;
    OutputStream outStream = null;
    
    try {
      File fileOutput = new File(dir + filename);
      
      inStream = new FileInputStream(inputFile);
      outStream = new FileOutputStream(fileOutput);
      
      byte[] buffer = new byte[1024];
      int length;
      
      while ((length = inStream.read(buffer)) > 0) {
        outStream.write(buffer, 0, length);
      }

      if (inStream != null) {
        inStream.close();
      }
      if (outStream != null) {
        outStream.flush();
        outStream.close();
      }      
      return true;
      
    } catch (IOException e) {      
      return false;
    }

  }
  

}
