 

package org.ats.generated;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
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

public class Jira {

  private RemoteWebDriver wd;

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
  public void test() throws Exception {
    wd.get("https://insight.fsoft.com.vn/jira/secure/Dashboard.jspa");

    wd = (FirefoxDriver) wd.switchTo().frame("gadget-0");

    wd.findElement(By.xpath(".//*[@id='login-form-username']")).click();
    wd.findElement(By.xpath(".//*[@id='login-form-username']")).sendKeys("trinhtv3");

    wd.findElement(By.xpath(".//*[@id='login-form-password']")).click();
    wd.findElement(By.xpath(".//*[@id='login-form-password']")).sendKeys("DamMai@65");

    if (!wd.findElement(By.xpath(".//*[@id='login-form-remember-me']")).isSelected()) {
      wd.findElement(By.xpath(".//*[@id='login-form-remember-me']")).click();
    }

    wd.findElement(By.xpath(".//*[@id='login']")).submit();

    try { Thread.sleep(2000l); } catch (Exception e) { throw new RuntimeException(e); }

    wd.get("https://insight.fsoft.com.vn/jira");

    assertTrue(wd.findElement(By.tagName("html")).getText().contains("Assigned to Me"));

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