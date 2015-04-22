/**
 * 
 */
package org.ats.services.functional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.ats.services.FunctionalModule;
import org.ats.services.functional.Suite.SuiteBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public class SuiteTemplateTestCase {
  
  private ActionFactory actionFactory;
  
  @BeforeMethod
  public void init() {
    Injector injector = Guice.createInjector(new FunctionalModule());
    this.actionFactory = injector.getInstance(ActionFactory.class);
  }

  @Test
  public void testFullExample() throws Exception {
    testBase("FullExample", "full_example.json");
  }
  
  @Test
  public void testVerifyEval() throws Exception {
    testBase("VerifyEval", "verifyEval.json");
  }
  
  @Test
  public void testAcceptAlert() throws Exception {
    testBase("AcceptAlert", "acceptAlert.json");
  }
  
  @Test
  public void testAnswerAlert() throws Exception {
    testBase("AnswerAlert", "answerAlert.json");
  }
  
  @Test
  public void testAssertAlertPresent() throws Exception {
    testBase("AssertAlertPresent", "assertAlertPresent.json");;
  }
  
  @Test
  public void testAssertAlertPresentFail() throws Exception {
    testBase("AssertAlertPresentFail", "assertAlertPresent_fail.json");
  }
  
  @Test
  public void testDismissAlert() throws Exception {
    testBase("DismissAlert", "dismissAlert.json");
  }
  
  @Test
  public void testNotAssertAlertPresent() throws Exception {
    testBase("NotAssertAlertPresent", "not_assertAlertPresent.json");
  }
  
  @Test
  public void testSwitchToFrame() throws Exception {
    testBase("SwitchToFrame", "switchToFrame.json");
  }
  
  @Test
  public void testSwitchToFrameByIndex() throws Exception {
    testBase("SwitchToFrameByIndex", "switchToFrameByIndex.json");
  }
  
  @Test
  public void testSwitchToWindow() throws Exception {
    testBase("SwitchToWindow", "switchToWindow.json");
  }
  
  @Test
  public void testJira() throws Exception {
    testBase("Jira", "jira.json");
  }
  
  private void testBase(String testClass, String jsonFile) throws JsonProcessingException, IOException {
    SuiteBuilder builder = new SuiteBuilder();
    builder.packageName("org.ats.generated")
      .suiteName(testClass)
      .driverVar(SuiteBuilder.DEFAULT_DRIVER_VAR)
      .initDriver(SuiteBuilder.DEFAULT_INIT_DRIVER)
      .timeoutSeconds(SuiteBuilder.DEFAULT_TIMEOUT_SECONDS);
    
    ObjectMapper m = new ObjectMapper();
    JsonNode rootNode = m.readTree(new File("src/test/resources/" + jsonFile));
    JsonNode stepsNode = rootNode.get("steps");
    Case caze = new Case("test");
    for (JsonNode json : stepsNode) {
      caze.addAction(actionFactory.createAction(json));
    }
    builder.addCases(caze);
    
    try {
      String output = builder.build().transform();
      FileWriter writer = new FileWriter(new File("src/test/java/org/ats/generated/" + testClass +".java"));
      writer.write(output);
      writer.close();
      
      System.out.println(output);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
}
