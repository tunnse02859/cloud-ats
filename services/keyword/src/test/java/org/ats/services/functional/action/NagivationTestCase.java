/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.DataDrivenModule;
import org.ats.services.KeywordServiceModule;
import org.ats.services.OrganizationServiceModule;
import org.ats.services.data.DatabaseModule;
import org.ats.services.event.EventModule;
import org.ats.services.keyword.ActionFactory;
import org.ats.services.keyword.action.AbstractAction;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public class NagivationTestCase {
  
  private ActionFactory actionFactory;
  
  private ObjectNode json;
  
  @BeforeClass
  public void init() throws Exception {
    Injector injector = Guice.createInjector(
        new DatabaseModule(), 
        new EventModule(),
        new OrganizationServiceModule(),
        new DataDrivenModule(),
        new KeywordServiceModule());
    
    this.actionFactory = injector.getInstance(ActionFactory.class);
    ObjectMapper mapper = new ObjectMapper();
    json = mapper.createObjectNode();
  }

  @Test
  public void testClose() throws IOException {
    json.put("type", "close");
    AbstractAction close = actionFactory.createAction(json);
    Assert.assertEquals(close.transform(), "wd.close();\n");
  }
  
  @Test
  public void testGet() throws IOException {
    json.put("type", "get");
    json.put("url", "http://saucelabs.com/test/guinea-pig/");
    AbstractAction get = actionFactory.createAction(json);
    Assert.assertEquals(get.transform(), "wd.get(\"http://saucelabs.com/test/guinea-pig/\");\n");
  }
  
  @Test
  public void testRefresh() throws IOException {
    json.put("type", "refresh");
    AbstractAction refresh = actionFactory.createAction(json);
    Assert.assertEquals(refresh.transform(), "wd.navigate().refresh();\n");
  }
 
  @Test
  public void testGoback() throws IOException {
    json.put("type", "goBack");
    AbstractAction goBack = actionFactory.createAction(json);
    Assert.assertEquals(goBack.transform(), "wd.navigate().back();\n");
  }
  
  @Test
  public void testGoForward() throws IOException {
    json.put("type", "goForward");
    AbstractAction goForward = actionFactory.createAction(json);
    Assert.assertEquals(goForward.transform(), "wd.navigate().forward();\n");
  }
}
