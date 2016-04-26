 

package org.ats.generated;

import java.text.ParseException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
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

public class SwitchToWindow {

  private RemoteWebDriver wd;
  
  @BeforeClass
  public void beforeClass() throws Exception {
  System.out.println("[Start][Suite]{\"name\": \"SwitchToWindow\", \"id\": \"21e33dca-c7bb-408a-9181-2dd766375e81\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
  }
   
  @AfterClass
  public void afterClass() throws Exception {
  System.out.println("[End][Suite]{\"name\": \"SwitchToWindow\", \"id\": \"21e33dca-c7bb-408a-9181-2dd766375e81\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
  }

  @BeforeMethod
  public void setUp() throws Exception {
    wd = new FirefoxDriver();
    wd.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
    wd.manage().window().maximize();
  }
   
  @AfterMethod
  public void tearDown() {
    wd.quit();
  }
  
  
  @Test
  public void test7e64366b() throws Exception {
    System.out.println("[Start][Case]{\"name\": \"test\", \"id\": \"7e64366b-c775-48d2-8c76-9517470629c7\", \"timestamp\": \""+System.currentTimeMillis()+"\"} "); 

    System.out.println("[Start][Step]{\"keyword_type\":\"get \",\"url\":\"http://seleniumbuilder.github.io/se-builder/test/window.html\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try {
wd.get("http://seleniumbuilder.github.io/se-builder/test/window.html");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_get.png"));
e.printStackTrace();
throw e ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"pause \",\"waittime\":\"1000ms\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"waittime\"]} "); 
    try {
 Thread.sleep(1000l);
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


    System.out.println("[Start][Step]{\"keyword_type\":\"assertTitle \",\"title\":\"Spawned window\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"title\"]} "); 
    try { 
assertEquals(     wd.getTitle(), "Spawned window");
     System.out.println("[End][Step]"); 
   } catch (AssertionError ae) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertTitle.png"));
     ae.printStackTrace();
     throw ae ; 
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