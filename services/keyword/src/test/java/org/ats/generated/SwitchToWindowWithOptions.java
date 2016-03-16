 

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

public class SwitchToWindowWithOptions {

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
  
  
  @Test(priority = 1)
  public void test846f9f3e() throws Exception {
    System.out.println("[INFO] Perform get url \"http://seleniumbuilder.github.io/se-builder/test/window.html\"");
    wd.get("http://seleniumbuilder.github.io/se-builder/test/window.html");

    System.out.println("[INFO] Waiting 5(s) for next step");
try { Thread.sleep(5000l); } catch (Exception e) { throw new RuntimeException(e); }
    System.out.println("[INFO] Perform pause wait time \"1000\"ms");
    try { Thread.sleep(1000l); } catch (Exception e) { throw new RuntimeException(e); }

    System.out.println("[INFO] Waiting 5(s) for next step");
try { Thread.sleep(5000l); } catch (Exception e) { throw new RuntimeException(e); }
    System.out.println("[INFO] Perform switchToWindow name \"win2\"");
    wd = (FirefoxDriver) wd.switchTo().window("win2");

    System.out.println("[INFO] Waiting 5(s) for next step");
try { Thread.sleep(5000l); } catch (Exception e) { throw new RuntimeException(e); }
    System.out.println("[INFO] Perform assertTitle title \"Spawned window\"");
    assertEquals(wd.getTitle(), "Spawned window");

    System.out.println("[INFO] Waiting 5(s) for next step");
try { Thread.sleep(5000l); } catch (Exception e) { throw new RuntimeException(e); }
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