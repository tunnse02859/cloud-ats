/**
 * 
 */
package org.ats.services.functional;

import java.io.File;
import java.io.FileWriter;

import org.ats.services.FunctionalModule;
import org.ats.services.functional.Suite.SuiteBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
  public void test() throws Exception {
    SuiteBuilder builder = new SuiteBuilder();
    builder.packageName("org.ats.generated")
      .suiteName("TestGenerated")
      .driverVar(SuiteBuilder.DEFAULT_DRIVER_VAR)
      .initDriver(SuiteBuilder.DEFAULT_INIT_DRIVER)
      .timeoutSeconds(SuiteBuilder.DEFAULT_TIMEOUT_SECONDS);
    
    ObjectMapper m = new ObjectMapper();
    JsonNode rootNode = m.readTree(new File("src/test/resources/full_example.json"));
    JsonNode stepsNode = rootNode.get("steps");
    Case caze = new Case("test");
    for (JsonNode json : stepsNode) {
//      builder.addCases(actionFactory.createAction(json));
      caze.addAction(actionFactory.createAction(json));
    }
    builder.addCases(caze);
    try {
      String output = builder.build().transform();
      FileWriter writer = new FileWriter(new File("src/test/java/org/ats/generated/TestGenerated.java"));
      writer.write(output);
      writer.close();
      System.out.println(output);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
}
