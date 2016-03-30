 

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

public class TestVNEpress {

  private RemoteWebDriver wd;
  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  long time = 0 ;
  
  @BeforeClass
  public void beforeClass() throws Exception {
  time = dateFormat.parse(dateFormat.format(new Date())).getTime();
  System.out.println("[Start][Suite]{\"name\": \"TestVNEpress\", \"id\": \"9cdc5daf-2b6c-438a-8991-96e4a574692e\", \"jobId\" : \"\", \"timestamp\": \""+time+"\"}");
  }
   
  @AfterClass
  public void afterClass() throws Exception {
  time = dateFormat.parse(dateFormat.format(new Date())).getTime();
  System.out.println("[End][Suite]{\"name\": \"TestVNEpress\", \"id\": \"9cdc5daf-2b6c-438a-8991-96e4a574692e\", \"jobId\" : \"\", \"timestamp\": \""+time+"\"}");
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
  public void test9d403762() throws Exception {
    time = dateFormat.parse(dateFormat.format(new Date())).getTime();
    System.out.println("[Start][Case]{\"name\": \"test\", \"id\": \"9d403762-59a1-4676-b01b-ac46dfad0b0a\", \"timeStamp\": \""+time+"\"} "); 
    System.out.println("[Start][Step]{\"keyword_type\":\"get \",\"url\":\"http://vnexpress.net/\",\"params\":[\"url\"]} "); 
    try {
wd.get("http://vnexpress.net/");
System.out.println("[End][Step]");
} catch (Exception e) {
time = dateFormat.parse(dateFormat.format(new Date())).getTime();
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+time+"_get.png"));
e.printStackTrace();
throw e ;
}

    System.out.println("[Start][Step]{\"keyword_type\":\"clickElement \",\"locator\":{\"type\":\"xpath\",\"value\":\".//*[@id='menu_web']//a[text()='Kinh doanh']\"},\"params\":[\"locator\"]} "); 
    try {
wd.findElement(By.xpath(".//*[@id='menu_web']//a[text()='Kinh doanh']")).click();
System.out.println("[End][Step]");
} catch (Exception e) {
time = dateFormat.parse(dateFormat.format(new Date())).getTime();
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+time+"_clickElement.png"));
e.printStackTrace();
throw e ;
}

    System.out.println("[Start][Step]{\"keyword_type\":\"verifyCurrentUrl \",\"url\":\"ABC\",\"params\":[\"url\"]} "); 
         time = dateFormat.parse(dateFormat.format(new Date())).getTime();
try { 
     if (     wd.getCurrentUrl().equals("ABC")) {
     System.out.println("Actual URL : "+wd.getCurrentUrl()); 
     System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+time+"_verifyCurrentUrl.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+time+"_verifyCurrentUrl.png"));
     e.printStackTrace();
     throw e ; 
   }

    System.out.println("[Start][Step]{\"keyword_type\":\"verifyText \",\"locator\":{\"type\":\"xpath\",\"value\":\".//*[@id='box_news_top']/div/div/div[1]/h1/a\"},\"text\":\"TuDH2\",\"params\":[\"locator\", \"text\"]} "); 
         time = dateFormat.parse(dateFormat.format(new Date())).getTime();
try { 
     if (wd.findElement(By.xpath(".//*[@id='box_news_top']/div/div/div[1]/h1/a")).getText().equals("TuDH2")) {
    System.out.println("Actual Text : "+wd.findElement(By.xpath(".//*[@id='box_news_top']/div/div/div[1]/h1/a")).getText()); 
    System.out.println("[End][Step]"); 
    } else {
     wd.getScreenshotAs(FILE).renameTo(new File("target/error"+time+"verifyText.png"));
    }
   } catch (Exception e) { 
     wd.getScreenshotAs(FILE).renameTo(new File("target/error"+time+"verifyText.png"));
     e.printStackTrace();
     throw e ; 
   }

    System.out.println("[Start][Step]{\"keyword_type\":\"clickElement \",\"locator\":{\"type\":\"xpath\",\"value\":\".//*[@id='box_news_top']/div/div/div[1]/h1/a\"},\"params\":[\"locator\"]} "); 
    try {
wd.findElement(By.xpath(".//*[@id='box_news_top']/div/div/div[1]/h1/a")).click();
System.out.println("[End][Step]");
} catch (Exception e) {
time = dateFormat.parse(dateFormat.format(new Date())).getTime();
wd.getScreenshotAs(FILE).renameTo(new File("target/error_"+time+"_clickElement.png"));
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