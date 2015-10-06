/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import static play.test.Helpers.GET;
import static play.test.Helpers.POST;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.routeAndCall;
import static play.test.Helpers.running;

import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;
import play.mvc.Result;
/**
 * @author TrinhTV3
 *
 */
public class DashboardTestCase {
  
  @Test
  public void testSummary() {
    
    running(fakeApplication(), new Runnable() {
      
      @Override
      public void run() {
        
        Result result = routeAndCall(fakeRequest(GET, "/api/v1/dashboard/summary"));
        
      }
    });
    
  }
}
