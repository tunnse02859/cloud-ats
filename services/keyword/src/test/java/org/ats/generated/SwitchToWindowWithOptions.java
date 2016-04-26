 

package org.ats.generated;

import java.text.ParseException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.*;
import static org.openqa.selenium.OutputType.*;

public class SwitchToWindowWithOptions {

  private RemoteWebDriver wd;

  @BeforeClass
  public void setUp() throws Exception {
    System.out.println("[Start][Suite]{\"name\": \"SwitchToWindowWithOptions\", \"id\": \"0414e326-02ae-4bbc-8de6-7aaef7a67f78\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
    wd = new FirefoxDriver();
    wd.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
    wd.manage().window().maximize();
  }
   
  @AfterClass
  public void tearDown() throws Exception {
    System.out.println("[End][Suite]{\"name\": \"SwitchToWindowWithOptions\", \"id\": \"0414e326-02ae-4bbc-8de6-7aaef7a67f78\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
    wd.quit();
  }
  
  
  @Test(priority = 1)
  public void test2456cab7() throws Exception {
    System.out.println("[Start][Case]{\"name\": \"test\", \"id\": \"2456cab7-512e-4a54-b034-bbec61f3be48\", \"timestamp\": \""+System.currentTimeMillis()+"\"} "); 

    System.out.println("[Start][Step]{\"keyword_type\":\"get \",\"url\":\"http://seleniumbuilder.github.io/se-builder/test/window.html\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try {
wd.get("http://seleniumbuilder.github.io/se-builder/test/window.html");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_get.png"));
e.printStackTrace();
throw e ;
}

    System.out.println("[INFO] Waiting 5(s) ");
try {
 Thread.sleep(5000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"pause \",\"waittime\":\"1000ms\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"waittime\"]} "); 
    try {
 Thread.sleep(1000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[INFO] Waiting 5(s) ");
try {
 Thread.sleep(5000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"switchToWindow \",\"name\":\"win2\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"name\"]} "); 
    try {
wd = (FirefoxDriver) wd.switchTo().window("win2");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_switchToWindow.png"));
e.printStackTrace();
throw e ;
}

    System.out.println("[INFO] Waiting 5(s) ");
try {
 Thread.sleep(5000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"assertTitle \",\"title\":\"Spawned window\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"title\"]} "); 
    try { 
assertEquals(     wd.getTitle(), "Spawned window");
     System.out.println("[End][Step]"); 
   } catch (AssertionError ae) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertTitle.png"));
     ae.printStackTrace();
     throw ae ; 
   }

    System.out.println("[INFO] Waiting 5(s) ");
try {
 Thread.sleep(5000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}
    System.out.println("[End][Case]"); 
  }

  public static boolean isAlertPresent(RemoteWebDriver wd) {
    try {
      wd.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }
}