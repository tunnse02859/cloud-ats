 

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

public class Google {

  private RemoteWebDriver wd;
  
  @BeforeClass
  public void beforeClass() throws Exception {
  System.out.println("[Start][Suite]{\"name\": \"Google\", \"id\": \"c1b969b0-e33d-4382-a25c-48fc347f1ee0\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
  }
   
  @AfterClass
  public void afterClass() throws Exception {
  System.out.println("[End][Suite]{\"name\": \"Google\", \"id\": \"c1b969b0-e33d-4382-a25c-48fc347f1ee0\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
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
  
  @DataProvider(name = "userSource72081f2a")
  public static Object[][] userSource72081f2a() throws Exception {
    ObjectMapper obj = new ObjectMapper();
    JsonNode rootNode = obj.readTree("[	{\"username\":\"foo\", \"UserName\": \"aaa\", \"Password\": \"bbb\", \"OrderId\": \"ccc\", \"textSearch\":\"aaa||bbb||ccc\"},	{\"username\":\"__blank__\", \"UserName\": \"aaa\", \"Password\": \"bbb\", \"OrderId\": \"ccc\", \"textSearch\":\"aaa||bbb||ccc\"}]");

    JsonNode[][] objData = new JsonNode[rootNode.size()][];
    for(int i=0; i<rootNode.size(); i++) {
      objData[i] = new JsonNode[]{ rootNode.get(i) };
    }
    return objData;
}
  @Test (dataProvider = "userSource72081f2a")
  public void test72081f2a(JsonNode data) throws Exception {
    System.out.println("[Start][Case]{\"name\": \"test\", \"id\": \"72081f2a-b320-47f4-b716-c5c0b5dd1068\", \"timestamp\": \""+System.currentTimeMillis()+"\"} "); 
    System.out.println("[Start][Data]"+data.toString()); 
    Object data_username = data.get("username");
    String username = null;
    if (data_username != null) {
        username = data_username.toString();
        username = username.substring(1, username.length() - 1).replace("\\\"","\"");
    }

    Object data_UserName = data.get("UserName");
    String UserName = null;
    if (data_UserName != null) {
        UserName = data_UserName.toString();
        UserName = UserName.substring(1, UserName.length() - 1).replace("\\\"","\"");
    }

    Object data_Password = data.get("Password");
    String Password = null;
    if (data_Password != null) {
        Password = data_Password.toString();
        Password = Password.substring(1, Password.length() - 1).replace("\\\"","\"");
    }

    Object data_OrderId = data.get("OrderId");
    String OrderId = null;
    if (data_OrderId != null) {
        OrderId = data_OrderId.toString();
        OrderId = OrderId.substring(1, OrderId.length() - 1).replace("\\\"","\"");
    }

    Object data_textSearch = data.get("textSearch");
    String textSearch = null;
    if (data_textSearch != null) {
        textSearch = data_textSearch.toString();
        textSearch = textSearch.substring(1, textSearch.length() - 1).replace("\\\"","\"");
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


    System.out.println("[Start][Step]{\"keyword_type\":\"sendKeysToElement \",\"locator\":{\"type\":\"xpath\",\"value\":\".//input[@id='lst-ib']\"},\"text\":\"${username}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try { 
     wd.findElement(By.xpath(".//input[@id='lst-ib']")).click();
     wd.findElement(By.xpath(".//input[@id='lst-ib']")).sendKeys(username);
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_sendKeysToElement.png"));
     e.printStackTrace();
     if ("__blank__".equals(username)) {
     System.out.println("[End][Step]");
     } else throw e;
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


    System.out.println("[Start][Step]{\"keyword_type\":\"pause \",\"waittime\":\"3000ms\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"waittime\"]} "); 
    try {
 Thread.sleep(3000);
    System.out.println("[End][Step]"); 
 } catch (Exception e) {
      e.printStackTrace();
     throw new RuntimeException(e);
}

try {
String[] array_OrderId = OrderId.split("\\|\\|");
String[] array_UserName = UserName.split("\\|\\|");
String[] array_Password = Password.split("\\|\\|");
for(int i = 0; i < 3; i++){
OrderId = array_OrderId[i];
UserName = array_UserName[i];
Password = array_Password[i];

    System.out.println("[Start][Step]{\"keyword_type\":\"sendKeysToElement \",\"locator\":{\"type\":\"id\",\"value\":\"${OrderId}\"},\"text\":\"${UserName}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try { 
     wd.findElement(By.id(OrderId)).click();
     wd.findElement(By.id(OrderId)).sendKeys(UserName);
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_sendKeysToElement.png"));
     e.printStackTrace();
     if ("__blank__".equals(UserName)) {
     System.out.println("[End][Step]");
     } else throw e;
   }

    System.out.println("[Start][Step]{\"keyword_type\":\"sendKeysToElement \",\"locator\":{\"type\":\"id\",\"value\":\"Password\"},\"text\":\"${Password}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try { 
     wd.findElement(By.id("Password")).click();
     wd.findElement(By.id("Password")).sendKeys(Password);
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_sendKeysToElement.png"));
     e.printStackTrace();
     if ("__blank__".equals(Password)) {
     System.out.println("[End][Step]");
     } else throw e;
   }

    System.out.println("[Start][Step]{\"keyword_type\":\"clickElement \",\"locator\":{\"type\":\"id\",\"value\":\"Submit\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try {
wd.findElement(By.id("Submit")).click();
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_clickElement.png"));
e.printStackTrace();
throw e ;
}
}
   } catch (Exception e) { 
e.printStackTrace();
throw e ; 
 }

try {
String[] array_textSearch = textSearch.split("\\|\\|");
for(int i = 0; i < 3; i++){
textSearch = array_textSearch[i];

    System.out.println("[Start][Step]{\"keyword_type\":\"sendKeysToElement \",\"locator\":{\"type\":\"xpath\",\"value\":\"//*[@id=\\\"lst-ib\\\"]\"},\"text\":\"${textSearch}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try { 
     wd.findElement(By.xpath("//*[@id=\"lst-ib\"]")).click();
     wd.findElement(By.xpath("//*[@id=\"lst-ib\"]")).sendKeys(textSearch);
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_sendKeysToElement.png"));
     e.printStackTrace();
     if ("__blank__".equals(textSearch)) {
     System.out.println("[End][Step]");
     } else throw e;
   }

    System.out.println("[Start][Step]{\"keyword_type\":\"clickElement \",\"locator\":{\"type\":\"name\",\"value\":\"btnG\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try {
wd.findElement(By.name("btnG")).click();
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_clickElement.png"));
e.printStackTrace();
throw e ;
}
}
   } catch (Exception e) { 
e.printStackTrace();
throw e ; 
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