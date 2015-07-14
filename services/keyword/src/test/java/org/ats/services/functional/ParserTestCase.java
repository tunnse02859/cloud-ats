/**
 * 
 */
package org.ats.services.functional;

import java.io.File;
import java.io.IOException;

import org.ats.services.DataDrivenModule;
import org.ats.services.FunctionalServiceModule;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.event.EventModule;
import org.ats.services.functional.action.AbstractAction;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 16, 2015
 */
public class ParserTestCase {

  private ActionFactory actionFactory;
  
  @BeforeClass
  public void init() throws Exception {
    Injector injector = Guice.createInjector(
        new DatabaseModule(), 
        new EventModule(),
        new OrganizationServiceModule(),
        new DataDrivenModule(),
        new FunctionalServiceModule());
    
    this.actionFactory = injector.getInstance(ActionFactory.class);
  }
  
  @Test
  public void test() throws JsonProcessingException, IOException {
    ObjectMapper m = new ObjectMapper();
    JsonNode rootNode = m.readTree(new File("src/test/resources/full_example.json"));
    JsonNode stepsNode = rootNode.get("steps");
    for (JsonNode json : stepsNode) {
      AbstractAction action = actionFactory.createAction(json);
      if (json.get("type").asText().startsWith("wait")) {
        Assert.assertNull(action);
      } else {
        DBObject dbObj = (DBObject) JSON.parse(json.toString());
        JsonNode serializeJson = m.readTree(dbObj.toString());
        Assert.assertEquals(serializeJson, json);
        Assert.assertNotNull(action);
      }
    }
  }
}
