 

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

public class FullExampleWithPriority {

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
  public void test936937c8() throws Exception {
    wd.get("http://saucelabs.com/test/guinea-pig/");

    wd.findElement(By.linkText("i am a link")).click();

    String title = wd.getTitle();

    if (!wd.getTitle().equals(title)) {
      System.out.println("verifyTitle failed");
    }

    if (wd.getTitle().equals("asdf")) {
      System.out.println("!verifyTitle failed");
    }

    assertEquals(wd.getTitle(), title);

    assertNotEquals(wd.getTitle(), "asdf");

    String url = wd.getCurrentUrl();

    if (!wd.getCurrentUrl().equals(url)) {
      System.out.println("verifyCurrentUrl failed");
    }

    if (wd.getCurrentUrl().equals("http://google.com")) {
      System.out.println("!verifyCurrentUrl failed");
    }

    assertEquals(wd.getCurrentUrl(), url);

    assertNotEquals(wd.getCurrentUrl(), "http://google.com");

    String text = wd.findElement(By.id("i_am_an_id")).getText();

    if (!wd.findElement(By.id("i_am_an_id")).getText().equals(text)) {
      System.out.println("verifyText failed");
    }

    if (wd.findElement(By.id("i_am_an_id")).getText().equals("not \" + text + \"")) {
      System.out.println("!verifyText failed");
    }

    assertEquals(wd.findElement(By.id("i_am_an_id")).getText(), text);

    assertNotEquals(wd.findElement(By.id("i_am_an_id")).getText(), "not \" + text + \"");

    boolean text_is_present = wd.findElement(By.tagName("html")).getText().contains("I am another div");

    String text_present = "I am another div";

    if (!wd.findElement(By.tagName("html")).getText().contains(text_present)) {
      System.out.println("verifyTextPresent failed");
    }

    if (wd.findElement(By.tagName("html")).getText().contains("not \" + text_present + \"")) {
      System.out.println("!verifyTextPresent failed");
    }

    assertTrue(wd.findElement(By.tagName("html")).getText().contains(text_present));

    assertFalse(wd.findElement(By.tagName("html")).getText().contains("not \" + text_present + \""));

    String body_text = wd.findElement(By.tagName("html")).getText();

    if (!wd.findElement(By.tagName("html")).getText().equals(body_text)) {
      System.out.println("verifyBodyText failed");
    }

    if (wd.findElement(By.tagName("html")).getText().equals("not \" + body_text + \"")) {
      System.out.println("!verifyBodyText failed");
    }

    assertEquals(wd.findElement(By.tagName("html")).getText(), body_text);

    assertNotEquals(wd.findElement(By.tagName("html")).getText(), "not \" + body_text + \"");

    String page_source = wd.getPageSource();

    if (!wd.getPageSource().equals(page_source)) {
      System.out.println("verifyPageSource failed");
    }

    if (wd.getPageSource().equals("<!-- --> \" + page_source + \"")) {
      System.out.println("!verifyPageSource failed");
    }

    assertEquals(wd.getPageSource(), page_source);

    assertNotEquals(wd.getPageSource(), "<!-- --> \" + page_source + \"");

    wd.manage().addCookie(new Cookie.Builder("test_cookie", "this-is-a-cookie").path("/").expiresOn(new Date(new Date().getTime() + 100000000000l)).build());

    boolean cookie_is_present = (wd.manage().getCookieNamed("test_cookie") != null);

    String cookie = wd.manage().getCookieNamed("test_cookie").getValue();

    System.out.println("\" + cookie + \";");

    if (!(wd.manage().getCookieNamed("test_cookie") != null)) {
      System.out.println("verifyCookiePresent failed");
    }

    assertTrue((wd.manage().getCookieNamed("test_cookie") != null));

    if (!wd.manage().getCookieNamed("test_cookie").getValue().equals(cookie)) {
      System.out.println("verifyCookieByName failed");
    }

    if (wd.manage().getCookieNamed("test_cookie").getValue().equals("not \" + cookie + \"")) {
      System.out.println("!verifyCookieByName failed");
    }

    assertEquals(wd.manage().getCookieNamed("test_cookie").getValue(), cookie);

    assertNotEquals(wd.manage().getCookieNamed("test_cookie").getValue(), "not \" + cookie + \"");

    if (wd.manage().getCookieNamed("test_cookie") != null) {
      wd.manage().deleteCookie(wd.manage().getCookieNamed("test_cookie"));
    }

    if ((wd.manage().getCookieNamed("test_cookie") != null)) {
      System.out.println("!verifyCookiePresent failed");
    }

    assertFalse((wd.manage().getCookieNamed("test_cookie") != null));

    wd.navigate().refresh();

    wd.navigate().back();

    wd.navigate().forward();

    wd.navigate().back();

    wd.getScreenshotAs(FILE).renameTo(new File("/tmp/screen.png"));

    System.out.println("this is some debug text");

    boolean element_is_selected = (wd.findElement(By.id("unchecked_checkbox")).isSelected());

    if (!wd.findElement(By.id("unchecked_checkbox")).isSelected()) {
      wd.findElement(By.id("unchecked_checkbox")).click();
    }

    if (!(wd.findElement(By.id("unchecked_checkbox")).isSelected())) {
      System.out.println("verifyElementSelected failed");
    }

    assertTrue((wd.findElement(By.id("unchecked_checkbox")).isSelected()));

    if (wd.findElement(By.id("unchecked_checkbox")).isSelected()) {
      wd.findElement(By.id("unchecked_checkbox")).click();
    }

    if ((wd.findElement(By.id("unchecked_checkbox")).isSelected())) {
      System.out.println("!verifyElementSelected failed");
    }

    assertFalse((wd.findElement(By.id("unchecked_checkbox")).isSelected()));

    String link_href = wd.findElement(By.linkText("i am a link")).getAttribute("href");

    if (!wd.findElement(By.linkText("i am a link")).getAttribute("href").equals(link_href)) {
      System.out.println("verifyElementAttribute failed");
    }

    assertEquals(wd.findElement(By.linkText("i am a link")).getAttribute("href"), link_href);

    wd.findElement(By.id("comments")).click();
    wd.findElement(By.id("comments")).sendKeys("w00t");

    if (wd.findElement(By.linkText("i am a link")).getAttribute("href").equals("not \" + link_href + \"")) {
      System.out.println("!verifyElementAttribute failed");
    }

    assertNotEquals(wd.findElement(By.linkText("i am a link")).getAttribute("href"), "not \" + link_href + \"");

    String comments = wd.findElement(By.id("comments")).getAttribute("value");

    if (!wd.findElement(By.id("comments")).getAttribute("value").equals("w00t")) {
      System.out.println("verifyElementValue failed");
    }

    assertEquals(wd.findElement(By.id("comments")).getAttribute("value"), "w00t");

    if (wd.findElement(By.id("comments")).getAttribute("value").equals("not w00t")) {
      System.out.println("!verifyElementValue failed");
    }

    assertNotEquals(wd.findElement(By.id("comments")).getAttribute("value"), "not w00t");

    wd.findElement(By.id("comments")).submit();

    if (!wd.findElement(By.tagName("html")).getText().contains("w00t")) {
      System.out.println("verifyTextPresent failed");
    }

  }
  @Test(priority = 2)
  public void test2d6f1436a() throws Exception {
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