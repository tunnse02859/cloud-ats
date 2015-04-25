 

package org.ats.generated;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.DataProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.io.File;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.*;
import static org.openqa.selenium.OutputType.*;

public class Google {

  FirefoxDriver wd;

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
  
  
  @Test (dataProvider = "LoadData")
  public void test(JsonNode data) throws Exception {
String username = data.get("username").toString().split("\"")[1];
String password = data.get("password").toString().split("\"")[1];
    wd.get("https://www.google.com/?gws_rd=ssl");

    wd.findElement(By.xpath(".//input[@id='lst-ib']")).click();
    wd.findElement(By.xpath(".//input[@id='lst-ib']")).sendKeys(username);

    wd.findElement(By.xpath(".//input[@id='lst-ib']")).submit();

  }


  public static boolean isAlertPresent(FirefoxDriver wd) {
    try {
      wd.switchTo().alert();
      return true;
    } catch (NoAlertPresentException e) {
      return false;
    }
  }
  
  @DataProvider(name = "LoadData")
public static Object[][] loadData() throws JsonProcessingException, IOException {
String DATA_PATH = "C:\\Users\\nambv2\\Desktop\\data.json";
ObjectMapper obj = new ObjectMapper();
JsonNode rootNode;
List<JsonNode> listData = new ArrayList<JsonNode>();

rootNode = obj.readTree(new File(DATA_PATH));
for(JsonNode json:rootNode) {
   listData.add(json);
}

JsonNode[][] objData = new JsonNode[listData.size()][];
for(int i=0; i<listData.size(); i++) {
   objData[i] = new JsonNode[]{listData.get(i)};
}
return objData;
}
  
}