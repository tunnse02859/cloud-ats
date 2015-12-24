 

package org.ats.generated;

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
import java.util.Date;
import java.io.File;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.*;
import static org.openqa.selenium.OutputType.*;

public class GoogleWithPriovity {

  private RemoteWebDriver wd;

  @BeforeClass
  public void setUp() throws Exception {
    wd = new FirefoxDriver();
    wd.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
    wd.manage().window().maximize();
  }
   
  @AfterClass
  public void tearDown() {
    wd.quit();
  }
  
  @DataProvider(name = "userSourcebc1fe76f")
  public static Object[][] userSourcebc1fe76f() throws Exception {
    ObjectMapper obj = new ObjectMapper();
    JsonNode rootNode = obj.readTree("[	{\"username\":\"foo\"},	{\"username\":\"foo1\"}]");

    JsonNode[][] objData = new JsonNode[rootNode.size()][];
    for(int i=0; i<rootNode.size(); i++) {
      objData[i] = new JsonNode[]{ rootNode.get(i) };
    }
    return objData;
}
  @Test (dataProvider = "userSourcebc1fe76f", priority =1)
  public void testbc1fe76f(JsonNode data) throws Exception {
    int length_username = data.get("username").toString().length();
    String username = data.get("username").toString().substring(1,length_username-1).replace("\\\"","\"");

    wd.get("https://www.google.com/?gws_rd=ssl");

    System.out.println("[INFO] Waiting 3(s) for next step");
try { Thread.sleep(3000l); } catch (Exception e) { throw new RuntimeException(e); }
    wd.findElement(By.xpath(".//input[@id='lst-ib']")).click();
    wd.findElement(By.xpath(".//input[@id='lst-ib']")).sendKeys(username);

    System.out.println("[INFO] Waiting 3(s) for next step");
try { Thread.sleep(3000l); } catch (Exception e) { throw new RuntimeException(e); }
    wd.findElement(By.xpath(".//input[@id='lst-ib']")).submit();

    System.out.println("[INFO] Waiting 3(s) for next step");
try { Thread.sleep(3000l); } catch (Exception e) { throw new RuntimeException(e); }
    try { Thread.sleep(3000l); } catch (Exception e) { throw new RuntimeException(e); }

    System.out.println("[INFO] Waiting 3(s) for next step");
try { Thread.sleep(3000l); } catch (Exception e) { throw new RuntimeException(e); }
  }@DataProvider(name = "userSourcec4d6df1a")
  public static Object[][] userSourcec4d6df1a() throws Exception {
    ObjectMapper obj = new ObjectMapper();
    JsonNode rootNode = obj.readTree("[	{\"username\":\"foo\"},	{\"username\":\"foo1\"}]");

    JsonNode[][] objData = new JsonNode[rootNode.size()][];
    for(int i=0; i<rootNode.size(); i++) {
      objData[i] = new JsonNode[]{ rootNode.get(i) };
    }
    return objData;
}
  @Test (dataProvider = "userSourcec4d6df1a", priority =2)
  public void test2c4d6df1a(JsonNode data) throws Exception {
    int length_username = data.get("username").toString().length();
    String username = data.get("username").toString().substring(1,length_username-1).replace("\\\"","\"");

    wd.get("https://www.google.com/?gws_rd=ssl");

    System.out.println("[INFO] Waiting 3(s) for next step");
try { Thread.sleep(3000l); } catch (Exception e) { throw new RuntimeException(e); }
    wd.findElement(By.xpath(".//input[@id='lst-ib']")).click();
    wd.findElement(By.xpath(".//input[@id='lst-ib']")).sendKeys(username);

    System.out.println("[INFO] Waiting 3(s) for next step");
try { Thread.sleep(3000l); } catch (Exception e) { throw new RuntimeException(e); }
    wd.findElement(By.xpath(".//input[@id='lst-ib']")).submit();

    System.out.println("[INFO] Waiting 3(s) for next step");
try { Thread.sleep(3000l); } catch (Exception e) { throw new RuntimeException(e); }
    try { Thread.sleep(3000l); } catch (Exception e) { throw new RuntimeException(e); }

    System.out.println("[INFO] Waiting 3(s) for next step");
try { Thread.sleep(3000l); } catch (Exception e) { throw new RuntimeException(e); }
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