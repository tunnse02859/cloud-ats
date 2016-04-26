 

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

public class FullExample {

  private RemoteWebDriver wd;
  
  @BeforeClass
  public void beforeClass() throws Exception {
  System.out.println("[Start][Suite]{\"name\": \"FullExample\", \"id\": \"faa1047c-b573-446c-bf05-db3e54f5beea\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
  }
   
  @AfterClass
  public void afterClass() throws Exception {
  System.out.println("[End][Suite]{\"name\": \"FullExample\", \"id\": \"faa1047c-b573-446c-bf05-db3e54f5beea\", \"jobId\" : \"\", \"timestamp\": \""+System.currentTimeMillis()+"\"}");
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
  public void teste6afce40() throws Exception {
    System.out.println("[Start][Case]{\"name\": \"test\", \"id\": \"e6afce40-2cc9-4d0d-91e6-fa74396205a7\", \"timestamp\": \""+System.currentTimeMillis()+"\"} "); 

    System.out.println("[Start][Step]{\"keyword_type\":\"get \",\"url\":\"http://saucelabs.com/test/guinea-pig/\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try {
wd.get("http://saucelabs.com/test/guinea-pig/");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_get.png"));
e.printStackTrace();
throw e ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"clickElement \",\"locator\":{\"type\":\"link text\",\"value\":\"i am a link\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try {
wd.findElement(By.linkText("i am a link")).click();
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_clickElement.png"));
e.printStackTrace();
throw e ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"storeTitle \",\"variable\":\"title\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"variable\"]} "); 
    String title = "";
try { 
title = wd.getTitle();
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_storeTitle.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyTitle \",\"title\":\"${title}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"title\"]} "); 
    try { 
     System.out.println("Actual Title : "+wd.getTitle()); 
     if (wd.getTitle().equals(title)) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyTitle.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyTitle.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyTitle \",\"title\":\"asdf\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"title\"]} "); 
    try { 
     System.out.println("Actual Title : "+wd.getTitle()); 
     if (!wd.getTitle().equals("asdf")) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyTitle.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyTitle.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertTitle \",\"title\":\"${title}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"title\"]} "); 
    try { 
assertEquals(     wd.getTitle(), title);
     System.out.println("[End][Step]"); 
   } catch (AssertionError ae) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertTitle.png"));
     ae.printStackTrace();
     throw ae ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertTitle \",\"title\":\"asdf\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"title\"]} "); 
    try { 
assertNotEquals(     wd.getTitle(), "asdf");
     System.out.println("[End][Step]"); 
   } catch (AssertionError ae) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertTitle.png"));
     ae.printStackTrace();
     throw ae ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"storeCurrentUrl \",\"variable\":\"url\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"variable\"]} "); 
    String url = "";
try { 
url = wd.getCurrentUrl();
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_storeCurrentUrl.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyCurrentUrl \",\"url\":\"${url}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try { 
     System.out.println("Actual URL : "+wd.getCurrentUrl()); 
     if (     wd.getCurrentUrl().equals(url)) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyCurrentUrl.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyCurrentUrl.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyCurrentUrl \",\"url\":\"http://google.com\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try { 
     System.out.println("Actual URL : "+wd.getCurrentUrl()); 
     if (!     wd.getCurrentUrl().equals("http://google.com")) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyCurrentUrl.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyCurrentUrl.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertCurrentUrl \",\"url\":\"${url}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try { 
assertEquals(     wd.getCurrentUrl(), url);
     System.out.println("[End][Step]"); 
   } catch (AssertionError ae) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertCurrentUrl.png"));
     ae.printStackTrace();
     throw ae ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertCurrentUrl \",\"url\":\"http://google.com\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"url\"]} "); 
    try { 
assertNotEquals(     wd.getCurrentUrl(), "http://google.com");
     System.out.println("[End][Step]"); 
   } catch (AssertionError ae) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertCurrentUrl.png"));
     ae.printStackTrace();
     throw ae ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"storeText \",\"locator\":{\"type\":\"id\",\"value\":\"i_am_an_id\"},\"variable\":\"text\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"variable\"]} "); 
    String text = "";
try {
text = wd.findElement(By.id("i_am_an_id")).getText();
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_storeText.png"));
e.printStackTrace();
throw e ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyText \",\"locator\":{\"type\":\"id\",\"value\":\"i_am_an_id\"},\"text\":\"${text}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try { 
    System.out.println("Actual Text : "+wd.findElement(By.id("i_am_an_id")).getText()); 
     if (wd.findElement(By.id("i_am_an_id")).getText().equals(text)) {
    System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyText.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyText.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyText \",\"locator\":{\"type\":\"id\",\"value\":\"i_am_an_id\"},\"text\":\"not ${text}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try { 
    System.out.println("Actual Text : "+wd.findElement(By.id("i_am_an_id")).getText()); 
     if (!wd.findElement(By.id("i_am_an_id")).getText().equals("not \" + text + \"")) {
    System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyText.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyText.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertText \",\"locator\":{\"type\":\"id\",\"value\":\"i_am_an_id\"},\"text\":\"${text}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try {
assertEquals( wd.findElement(By.id("i_am_an_id")).getText(), text);
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertText.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"assertText \",\"locator\":{\"type\":\"id\",\"value\":\"i_am_an_id\"},\"text\":\"not ${text}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try {
assertNotEquals( wd.findElement(By.id("i_am_an_id")).getText(), "not \" + text + \"");
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertText.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"storeTextPresent \",\"text\":\"I am another div\",\"variable\":\"text_is_present\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\", \"variable\"]} "); 
    boolean text_is_present = true;
try {
text_is_present = wd.findElement(By.tagName("html")).getText().contains("I am another div");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error"+System.currentTimeMillis()+"_storeTextPresent.png"));
e.printStackTrace();
throw e ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"store \",\"text\":\"I am another div\",\"variable\":\"text_present\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\", \"variable\"]} "); 
    String text_present = "";
try { 
text_present = "I am another div";
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_store.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyTextPresent \",\"text\":\"${text_present}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    try { 
     if (wd.findElement(By.tagName("html")).getText().contains(text_present)) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyTextPresent.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyTextPresent.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyTextPresent \",\"text\":\"not ${text_present}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    try { 
     if (!wd.findElement(By.tagName("html")).getText().contains("not \" + text_present + \"")) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyTextPresent.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyTextPresent.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertTextPresent \",\"text\":\"${text_present}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    try {
assertTrue( wd.findElement(By.tagName("html")).getText().contains(text_present));
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertTextPresent.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"assertTextPresent \",\"text\":\"not ${text_present}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    try {
assertFalse( wd.findElement(By.tagName("html")).getText().contains("not \" + text_present + \""));
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertTextPresent.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"storeBodyText \",\"variable\":\"body_text\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"variable\"]} "); 
    String body_text = "";
try { 
body_text = wd.findElement(By.tagName("html")).getText();
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_storeBodyText.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyBodyText \",\"text\":\"${body_text}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    try { 
     if (     wd.findElement(By.tagName("html")).getText().equals(body_text)) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyBodyText.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyBodyText.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyBodyText \",\"text\":\"not ${body_text}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    try { 
     if (!     wd.findElement(By.tagName("html")).getText().equals("not \" + body_text + \"")) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyBodyText.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyBodyText.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertBodyText \",\"text\":\"${body_text}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    try {
assertEquals( wd.findElement(By.tagName("html")).getText(), body_text);
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertBodyText.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"assertBodyText \",\"text\":\"not ${body_text}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    try {
assertNotEquals( wd.findElement(By.tagName("html")).getText(), "not \" + body_text + \"");
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertBodyText.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"storePageSource \",\"variable\":\"page_source\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"variable\"]} "); 
    String page_source = wd.getPageSource();
     System.out.println("[End][Step]"); 


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyPageSource \",\"source\":\"${page_source}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"source\"]} "); 
    try {
if (wd.getPageSource().equals(page_source)) {
     System.out.println("[End][Step]"); 
    }
 } catch (Exception e) { 
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyPageSource \",\"source\":\"<!-- --> ${page_source}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"source\"]} "); 
    try {
if (!wd.getPageSource().equals("<!-- --> \" + page_source + \"")) {
     System.out.println("[End][Step]"); 
    }
 } catch (Exception e) { 
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertPageSource \",\"source\":\"${page_source}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"source\"]} "); 
    try { 
assertEquals(     wd.getPageSource(), page_source);
     System.out.println("[End][Step]"); 
   } catch (AssertionError ae) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertPageSource.png"));
     ae.printStackTrace();
     throw ae ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertPageSource \",\"source\":\"<!-- --> ${page_source}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"source\"]} "); 
    try { 
assertNotEquals(     wd.getPageSource(), "<!-- --> \" + page_source + \"");
     System.out.println("[End][Step]"); 
   } catch (AssertionError ae) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertPageSource.png"));
     ae.printStackTrace();
     throw ae ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"addCookie \",\"name\":\"test_cookie\",\"options\":\"path=/,max_age=100000000\",\"value\":\"this-is-a-cookie\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"options\", \"name\", \"value\"]} "); 
    wd.manage().addCookie(new Cookie.Builder("test_cookie", "this-is-a-cookie").path("/").expiresOn(new Date(new Date().getTime() + 100000000000l)).build());


    System.out.println("[Start][Step]{\"keyword_type\":\"storeCookiePresent \",\"name\":\"test_cookie\",\"variable\":\"cookie_is_present\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"variable\", \"name\"]} "); 
    boolean cookie_is_present = (wd.manage().getCookieNamed("test_cookie") != null);
     System.out.println("[End][Step]"); 


    System.out.println("[Start][Step]{\"keyword_type\":\"storeCookieByName \",\"name\":\"test_cookie\",\"variable\":\"cookie\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"variable\", \"name\"]} "); 
    String cookie = wd.manage().getCookieNamed("test_cookie").getValue();
System.out.println("[End][Step]");


    System.out.println("[Start][Step]{\"keyword_type\":\"print \",\"text\":\"${cookie};\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    System.out.println("\" + cookie + \";");
System.out.println("[End][Step]");


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyCookiePresent \",\"name\":\"test_cookie\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"name\"]} "); 
    if ((wd.manage().getCookieNamed("test_cookie") != null)) {
     System.out.println("[End][Step]"); 
    }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertCookiePresent \",\"name\":\"test_cookie\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"name\"]} "); 
    try { 
assertTrue(     (wd.manage().getCookieNamed("test_cookie") != null));
     System.out.println("[End][Step]"); 
   } catch (AssertionError ae) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertCookiePresent.png"));
     ae.printStackTrace();
     throw ae ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyCookieByName \",\"name\":\"test_cookie\",\"value\":\"${cookie}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"name\", \"value\"]} "); 
    if (wd.manage().getCookieNamed("test_cookie").getValue().equals(cookie)) {
     System.out.println("[End][Step]"); 
    }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyCookieByName \",\"name\":\"test_cookie\",\"value\":\"not ${cookie}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"name\", \"value\"]} "); 
    if (!wd.manage().getCookieNamed("test_cookie").getValue().equals("not \" + cookie + \"")) {
     System.out.println("[End][Step]"); 
    }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertCookieByName \",\"name\":\"test_cookie\",\"value\":\"${cookie}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"name\", \"value\"]} "); 
    try {
assertEquals( wd.manage().getCookieNamed("test_cookie").getValue(), cookie);
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertCookieByName.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"assertCookieByName \",\"name\":\"test_cookie\",\"value\":\"not ${cookie}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"name\", \"value\"]} "); 
    try {
assertNotEquals( wd.manage().getCookieNamed("test_cookie").getValue(), "not \" + cookie + \"");
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertCookieByName.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"deleteCookie \",\"name\":\"test_cookie\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"name\"]} "); 
    try { 
if (wd.manage().getCookieNamed("test_cookie") != null) {
      wd.manage().deleteCookie(wd.manage().getCookieNamed("test_cookie"));
    }
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyCookiePresent \",\"name\":\"test_cookie\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"name\"]} "); 
    if (!(wd.manage().getCookieNamed("test_cookie") != null)) {
     System.out.println("[End][Step]"); 
    }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertCookiePresent \",\"name\":\"test_cookie\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"name\"]} "); 
    try { 
assertFalse(     (wd.manage().getCookieNamed("test_cookie") != null));
     System.out.println("[End][Step]"); 
   } catch (AssertionError ae) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertCookiePresent.png"));
     ae.printStackTrace();
     throw ae ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"refresh \",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[]} "); 
    try { 
     wd.navigate().refresh();
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_refresh.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"goBack \",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[]} "); 
    try { 
     wd.navigate().back();
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_goBack.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"goForward \",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[]} "); 
    try { 
     wd.navigate().forward();
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_goForward.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"goBack \",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[]} "); 
    try { 
     wd.navigate().back();
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_goBack.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"saveScreenshot \",\"file\":\"/tmp/screen.png\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"file\"]} "); 
    wd.getScreenshotAs(FILE).renameTo(new File("/tmp/screen.png"));
     System.out.println("[End][Step]"); 


    System.out.println("[Start][Step]{\"keyword_type\":\"print \",\"text\":\"this is some debug text\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    System.out.println("this is some debug text");
System.out.println("[End][Step]");


    System.out.println("[Start][Step]{\"keyword_type\":\"storeElementSelected \",\"locator\":{\"type\":\"id\",\"value\":\"unchecked_checkbox\"},\"variable\":\"element_is_selected\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"variable\"]} "); 
    boolean element_is_selected = true;
try {
element_is_selected = (wd.findElement(By.id("unchecked_checkbox")).isSelected());
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_storeElementSelected.png"));
e.printStackTrace();
throw e ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"setElementSelected \",\"locator\":{\"type\":\"id\",\"value\":\"unchecked_checkbox\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try { 
     if (!wd.findElement(By.id("unchecked_checkbox")).isSelected()) {
     wd.findElement(By.id("unchecked_checkbox")).click();
     }
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_setElementSelected.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyElementSelected \",\"locator\":{\"type\":\"id\",\"value\":\"unchecked_checkbox\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try { 
     if ((wd.findElement(By.id("unchecked_checkbox")).isSelected())) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementSelected.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementSelected.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertElementSelected \",\"locator\":{\"type\":\"id\",\"value\":\"unchecked_checkbox\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try {
assertTrue((wd.findElement(By.id("unchecked_checkbox")).isSelected()));
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertElementSelected.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"setElementNotSelected \",\"locator\":{\"type\":\"id\",\"value\":\"unchecked_checkbox\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try { 
     if (wd.findElement(By.id("unchecked_checkbox")).isSelected()) {
      wd.findElement(By.id("unchecked_checkbox")).click();
     }
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_setElementNotSelected.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyElementSelected \",\"locator\":{\"type\":\"id\",\"value\":\"unchecked_checkbox\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try { 
     if (!(wd.findElement(By.id("unchecked_checkbox")).isSelected())) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementSelected.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementSelected.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertElementSelected \",\"locator\":{\"type\":\"id\",\"value\":\"unchecked_checkbox\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try {
assertFalse((wd.findElement(By.id("unchecked_checkbox")).isSelected()));
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertElementSelected.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"storeElementAttribute \",\"locator\":{\"type\":\"link text\",\"value\":\"i am a link\"},\"attribute_name\":\"href\",\"variable\":\"link_href\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"variable\", \"attribute_name\"]} "); 
    String link_href = "";
try {
link_href = wd.findElement(By.linkText("i am a link")).getAttribute("href");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_storeElementAttribute.png"));
e.printStackTrace();
throw e ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyElementAttribute \",\"locator\":{\"type\":\"link text\",\"value\":\"i am a link\"},\"attribute_name\":\"href\",\"value\":\"${link_href}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"attribute_name\", \"value\"]} "); 
    try { 
     if (     wd.findElement(By.linkText("i am a link")).getAttribute("href").equals(link_href)) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementAttribute.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementAttribute.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertElementAttribute \",\"locator\":{\"type\":\"link text\",\"value\":\"i am a link\"},\"attribute_name\":\"href\",\"value\":\"${link_href}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"attribute_name\", \"value\"]} "); 
    try {
assertEquals( wd.findElement(By.linkText("i am a link")).getAttribute("href"), link_href);
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertElementAttribute.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"sendKeysToElement \",\"locator\":{\"type\":\"id\",\"value\":\"comments\"},\"text\":\"w00t\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"text\"]} "); 
    try { 
     wd.findElement(By.id("comments")).click();
     wd.findElement(By.id("comments")).sendKeys("w00t");
     System.out.println("[End][Step]"); 
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_sendKeysToElement.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyElementAttribute \",\"locator\":{\"type\":\"link text\",\"value\":\"i am a link\"},\"attribute_name\":\"href\",\"value\":\"not ${link_href}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"attribute_name\", \"value\"]} "); 
    try { 
     if (!     wd.findElement(By.linkText("i am a link")).getAttribute("href").equals("not \" + link_href + \"")) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementAttribute.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementAttribute.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertElementAttribute \",\"locator\":{\"type\":\"link text\",\"value\":\"i am a link\"},\"attribute_name\":\"href\",\"value\":\"not ${link_href}\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"attribute_name\", \"value\"]} "); 
    try {
assertNotEquals( wd.findElement(By.linkText("i am a link")).getAttribute("href"), "not \" + link_href + \"");
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertElementAttribute.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"storeElementValue \",\"locator\":{\"type\":\"id\",\"value\":\"comments\"},\"variable\":\"comments\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"variable\"]} "); 
    String comments = "";
try {
comments = wd.findElement(By.id("comments")).getAttribute("value");
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_storeElementValue.png"));
e.printStackTrace();
throw e ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyElementValue \",\"locator\":{\"type\":\"id\",\"value\":\"comments\"},\"value\":\"w00t\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"value\"]} "); 
    try { 
    System.out.println("Actual Value : " + wd.findElement(By.id("comments")).getAttribute("value")); 
     if (wd.findElement(By.id("comments")).getAttribute("value").equals("w00t")) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementValue.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementValue.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertElementValue \",\"locator\":{\"type\":\"id\",\"value\":\"comments\"},\"value\":\"w00t\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"value\"]} "); 
    try {
assertEquals( wd.findElement(By.id("comments")).getAttribute("value"), "w00t");
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertElementValue.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyElementValue \",\"locator\":{\"type\":\"id\",\"value\":\"comments\"},\"value\":\"not w00t\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"value\"]} "); 
    try { 
    System.out.println("Actual Value : " + wd.findElement(By.id("comments")).getAttribute("value")); 
     if (!wd.findElement(By.id("comments")).getAttribute("value").equals("not w00t")) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementValue.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyElementValue.png"));
     e.printStackTrace();
     throw e ; 
   }


    System.out.println("[Start][Step]{\"keyword_type\":\"assertElementValue \",\"locator\":{\"type\":\"id\",\"value\":\"comments\"},\"value\":\"not w00t\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\", \"value\"]} "); 
    try {
assertNotEquals( wd.findElement(By.id("comments")).getAttribute("value"), "not w00t");
System.out.println("[End][Step]");
} catch (AssertionError ae) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_assertElementValue.png"));
ae.printStackTrace();
throw ae ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"submitElement \",\"locator\":{\"type\":\"id\",\"value\":\"comments\"},\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"locator\"]} "); 
    try {
wd.findElement(By.id("comments")).submit();
System.out.println("[End][Step]");
} catch (Exception e) {
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_submitElement.png"));
e.printStackTrace();
throw e ;
}


    System.out.println("[Start][Step]{\"keyword_type\":\"verifyTextPresent \",\"text\":\"w00t\",\"timestamp\": \""+System.currentTimeMillis()+"\",\"params\":[\"text\"]} "); 
    try { 
     if (wd.findElement(By.tagName("html")).getText().contains("w00t")) {
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyTextPresent.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+System.currentTimeMillis()+"_verifyTextPresent.png"));
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