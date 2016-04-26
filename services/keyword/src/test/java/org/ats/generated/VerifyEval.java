 

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

public class VerifyEval {

  private RemoteWebDriver wd;
  
  @BeforeClass
  public void beforeClass() throws Exception {
  System.out.println("[Start][Suite]{\"name\": \"VerifyEval\", \"id\": \"bdfd0a01-54b5-4a7f-b5d9-063785ee200d\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
  }
   
  @AfterClass
  public void afterClass() throws Exception {
  System.out.println("[End][Suite]{\"name\": \"VerifyEval\", \"id\": \"bdfd0a01-54b5-4a7f-b5d9-063785ee200d\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
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
  public void test54638169() throws Exception {
    System.out.println("[Start][Case]{\"name\": \"test\", \"id\": \"54638169-e09e-427e-88c1-87e394e8839f\", \"timestamp\": \""+System.currentTimeMillis()+"\"} "); 

    System.out.println("[Start][Step]{\"keyword_type\":\"get \",\"url\":\"http://saucelabs.com/test/guinea-pig\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try {
wd.get("http://saucelabs.com/test/guinea-pig");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_get.png"));
e.printStackTrace();
throw e ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyElementPresent \",\"locator\":{\"type\":\"css selector\",\"value\":\"#i_am_an_id\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try { 
     if (     wd.findElements(By.cssSelector("#i_am_an_id")).size() != 0) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementPresent.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error"+System.currentTimeMillis()+"_verifyElementPresent.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"storeEval \",\"script\":\"return document.querySelectorAll('body div').length\",\"variable\":\"num_of_div_elements\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"variable\", \"script\"]} "); 
    Object num_of_div_elements = wd.executeScript("return document.querySelectorAll('body div').length");
System.out.println("[End][Step]");


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyEval \",\"script\":\"return ${num_of_div_elements}\",\"value\":\"7\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"script\", \"value\"]} "); 
    try {
if (wd.executeScript("return \" + num_of_div_elements + \"").equals("7")) {
     System.out.println("[End][Step]"); 
    }
 } catch (Exception e) { 
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyEval \",\"script\":\"return document.querySelectorAll('body div').length >= 7\",\"value\":\"true\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"script\", \"value\"]} "); 
    try {
if (wd.executeScript("return document.querySelectorAll('body div').length >= 7").equals("true")) {
     System.out.println("[End][Step]"); 
    }
 } catch (Exception e) { 
     e.printStackTrace();
     throw e ; 
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