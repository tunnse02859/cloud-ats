/**
 * 
 */
package org.ats.services.functional.action;

import java.io.IOException;

import org.ats.services.FunctionalModule;
import org.ats.services.functional.ActionFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
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
  
  @BeforeMethod
  public void init() {
    Injector injector = Guice.createInjector(new FunctionalModule());
    this.actionFactory = injector.getInstance(ActionFactory.class);
    ObjectMapper mapper = new ObjectMapper();
    json = mapper.createObjectNode();
  }

  @Test
  public void testClose() throws IOException {
    json.put("type", "close");
    IAction close = actionFactory.createAction(json);
    Assert.assertEquals(close.transform(), "wd.close();\n");
  }
  
  @Test
  public void testGet() throws IOException {
    json.put("type", "get");
    json.put("url", "http://saucelabs.com/test/guinea-pig/");
    IAction get = actionFactory.createAction(json);
    Assert.assertEquals(get.transform(), "wd.get(\"http://saucelabs.com/test/guinea-pig/\");\n");
  }
  
  @Test
  public void testRefresh() throws IOException {
    json.put("type", "refresh");
    IAction refresh = actionFactory.createAction(json);
    Assert.assertEquals(refresh.transform(), "wd.navigate().refresh();\n");
  }
 
  @Test
  public void testGoback() throws IOException {
    json.put("type", "goBack");
    IAction goBack = actionFactory.createAction(json);
    Assert.assertEquals(goBack.transform(), "wd.navigate().back();\n");
  }
  
  @Test
  public void testGoForward() throws IOException {
    json.put("type", "goForward");
    IAction goForward = actionFactory.createAction(json);
    Assert.assertEquals(goForward.transform(), "wd.navigate().forward();\n");
  }
}
