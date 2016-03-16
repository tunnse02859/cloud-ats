 

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

public class VerifyEval {

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
  public void test5d8f54d5() throws Exception {
    wd.get("http://saucelabs.com/test/guinea-pig");

    if (!(wd.findElements(By.cssSelector("#i_am_an_id")).size() != 0)) {
      System.out.println("verifyElementPresent failed");
    }

    Object num_of_div_elements = wd.executeScript("return document.querySelectorAll('body div').length");

    if (!wd.executeScript("return \" + num_of_div_elements + \"").equals("7")) {
      System.out.println("verifyEval failed");
    }

    if (!wd.executeScript("return document.querySelectorAll('body div').length >= 7").equals("true")) {
      System.out.println("verifyEval failed");
    }

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