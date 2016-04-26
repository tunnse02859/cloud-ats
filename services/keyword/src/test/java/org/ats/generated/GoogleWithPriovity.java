 

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

public class GoogleWithPriovity {

  private RemoteWebDriver wd;

  @BeforeClass
  public void setUp() throws Exception {
    System.out.println("[Start][Suite]{\"name\": \"GoogleWithPriovity\", \"id\": \"5a777b6a-527b-454b-a323-6e58b18c44b8\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
    wd = new FirefoxDriver();
    wd.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
    wd.manage().window().maximize();
  }
   
  @AfterClass
  public void tearDown() throws Exception {
    System.out.println("[End][Suite]{\"name\": \"GoogleWithPriovity\", \"id\": \"5a777b6a-527b-454b-a323-6e58b18c44b8\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
    wd.quit();
  }
  
  @DataProvider(name = "userSourced61df100")
  public static Object[][] userSourced61df100() throws Exception {
    ObjectMapper obj = new ObjectMapper();
    JsonNode rootNode = obj.readTree("[	{\"username\":\"foo\"},	{\"username\":\"foo1\"}]");

    JsonNode[][] objData = new JsonNode[rootNode.size()][];
    for(int i=0; i<rootNode.size(); i++) {
      objData[i] = new JsonNode[]{ rootNode.get(i) };
    }
    return objData;
}
  @Test (dataProvider = "userSourced61df100", priority = 1)
  public void testd61df100(JsonNode data) throws Exception {
    System.out.println("[Start][Case]{\"name\": \"test\", \"id\": \"d61df100-9fc4-4769-9a22-9683f94c3e0e\", \"timestamp\": \""+System.currentTimeMillis()+"\"} "); 
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
  }@DataProvider(name = "userSource75e03504")
  public static Object[][] userSource75e03504() throws Exception {
    ObjectMapper obj = new ObjectMapper();
    JsonNode rootNode = obj.readTree("[	{\"username\":\"foo\"},	{\"username\":\"foo1\"}]");

    JsonNode[][] objData = new JsonNode[rootNode.size()][];
    for(int i=0; i<rootNode.size(); i++) {
      objData[i] = new JsonNode[]{ rootNode.get(i) };
    }
    return objData;
}
  @Test (dataProvider = "userSource75e03504", priority = 2)
  public void test275e03504(JsonNode data) throws Exception {
    System.out.println("[Start][Case]{\"name\": \"test2\", \"id\": \"75e03504-eaff-498e-ab50-74d1dcf20473\", \"timestamp\": \""+System.currentTimeMillis()+"\"} "); 
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

  public static boolean isAlertPresent(RemoteWebDriver wd) {
    try {
      wd.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }
}