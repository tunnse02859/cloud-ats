 

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

public class GoogleMultiCasesWithPriovity {

  private RemoteWebDriver wd;

  @BeforeClass
  public void setUp() throws Exception {
    System.out.println("[Start][Suite]{\"name\": \"GoogleMultiCasesWithPriovity\", \"id\": \"c5cb49b3-22a1-40b9-a657-8affd04e8704\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
    wd = new FirefoxDriver();
    wd.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
    wd.manage().window().maximize();
  }
   
  @AfterClass
  public void tearDown() throws Exception {
    System.out.println("[End][Suite]{\"name\": \"GoogleMultiCasesWithPriovity\", \"id\": \"c5cb49b3-22a1-40b9-a657-8affd04e8704\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
    wd.quit();
  }
  
  @DataProvider(name = "userSourcef993d37b")
  public static Object[][] userSourcef993d37b() throws Exception {
    ObjectMapper obj = new ObjectMapper();
    JsonNode rootNode = obj.readTree("[	{\"username\":\"foo\"},	{\"username\":\"foo1\"}]");

    JsonNode[][] objData = new JsonNode[rootNode.size()][];
    for(int i=0; i<rootNode.size(); i++) {
      objData[i] = new JsonNode[]{ rootNode.get(i) };
    }
    return objData;
}
  @Test (dataProvider = "userSourcef993d37b", priority = 1)
  public void testf993d37b(JsonNode data) throws Exception {
    System.out.println("[Start][Case]{\"name\": \"test\", \"id\": \"f993d37b-bec3-43af-ad14-941bd68d0b12\", \"timestamp\": \""+System.currentTimeMillis()+"\"} "); 
    System.out.println("[Start][Data]"+data.toString()); 
    Object data_username = data.get("username");
    String username = null;
    if (data_username != null) {
        username = data_username.toString();
        username = username.substring(1, username.length() - 1).replace("\\\"","\"");
    }


    System.out.println("[Start][Step]{\"keyword_type\":\"get \",\"url\":\"https://www.google.com/?gws_rd=ssl\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try {
wd.get("https://www.google.com/?gws_rd=ssl");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_get.png"));
e.printStackTrace();
throw e ;
}

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"sendKeysToElement \",\"locator\":{\"type\":\"xpath\",\"value\":\".//input[@id='lst-ib']\"},\"text\":\"${username}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try { 
     wd.findElement(By.xpath(".//input[@id='lst-ib']")).click();
     wd.findElement(By.xpath(".//input[@id='lst-ib']")).sendKeys(username);
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_sendKeysToElement.png"));
     e.printStackTrace();
     throw e ; 
   }

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"submitElement \",\"locator\":{\"type\":\"xpath\",\"value\":\".//input[@id='lst-ib']\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try {
wd.findElement(By.xpath(".//input[@id='lst-ib']")).submit();
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_submitElement.png"));
e.printStackTrace();
throw e ;
}

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"pause \",\"waittime\":\"3000ms\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"waittime\"]} "); 
    try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}
System.out.println("[End][Data]"); 
    System.out.println("[End][Case]"); 
  }
  @Test(priority = 2)
  public void test2045098e2() throws Exception {
    System.out.println("[Start][Case]{\"name\": \"test2\", \"id\": \"045098e2-8c0d-4db2-b334-b7c738728239\", \"timestamp\": \""+System.currentTimeMillis()+"\"} "); 

    System.out.println("[Start][Step]{\"keyword_type\":\"get \",\"url\":\"https://insight.fsoft.com.vn/jira/secure/Dashboard.jspa\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try {
wd.get("https://insight.fsoft.com.vn/jira/secure/Dashboard.jspa");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_get.png"));
e.printStackTrace();
throw e ;
}

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"switchToFrame \",\"identifier\":\"gadget-0\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"identifier\"]} "); 
    try {
wd = (FirefoxDriver) wd.switchTo().frame("gadget-0");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_switchToFrame.png"));
e.printStackTrace();
throw e ;
}

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"sendKeysToElement \",\"locator\":{\"type\":\"xpath\",\"value\":\".//*[@id='login-form-username']\"},\"text\":\"trinhtv3\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try { 
     wd.findElement(By.xpath(".//*[@id='login-form-username']")).click();
     wd.findElement(By.xpath(".//*[@id='login-form-username']")).sendKeys("trinhtv3");
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_sendKeysToElement.png"));
     e.printStackTrace();
     throw e ; 
   }

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"sendKeysToElement \",\"locator\":{\"type\":\"xpath\",\"value\":\".//*[@id='login-form-password']\"},\"text\":\"DamMai@65\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try { 
     wd.findElement(By.xpath(".//*[@id='login-form-password']")).click();
     wd.findElement(By.xpath(".//*[@id='login-form-password']")).sendKeys("DamMai@65");
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_sendKeysToElement.png"));
     e.printStackTrace();
     throw e ; 
   }

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"setElementSelected \",\"locator\":{\"type\":\"xpath\",\"value\":\".//*[@id='login-form-remember-me']\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try { 
     if (!wd.findElement(By.xpath(".//*[@id='login-form-remember-me']")).isSelected()) {
     wd.findElement(By.xpath(".//*[@id='login-form-remember-me']")).click();
     }
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_setElementSelected.png"));
     e.printStackTrace();
     throw e ; 
   }

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"submitElement \",\"locator\":{\"type\":\"xpath\",\"value\":\".//*[@id='login']\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try {
wd.findElement(By.xpath(".//*[@id='login']")).submit();
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_submitElement.png"));
e.printStackTrace();
throw e ;
}

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"pause \",\"waittime\":\"2000ms\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"waittime\"]} "); 
    try {
 Thread.sleep(2000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"get \",\"url\":\"https://insight.fsoft.com.vn/jira\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try {
wd.get("https://insight.fsoft.com.vn/jira");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_get.png"));
e.printStackTrace();
throw e ;
}

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
     System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

    System.out.println("[Start][Step]{\"keyword_type\":\"assertTextPresent \",\"text\":\"Assigned to Me\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    try {
assertTrue( wd.findElement(By.tagName("html")).getText().contains("Assigned to Me"));
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertTextPresent.png"));
ae.printStackTrace();
throw ae ;
}

    System.out.println("[INFO] Waiting 3(s) ");
try {
 Thread.sleep(3000l);
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