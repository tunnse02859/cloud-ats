/**
 * 
 */
package webui;

import java.lang.reflect.Method;
import java.util.Map;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.EntryPoint;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Nov 24, 2014
 */
public class EventController extends Controller {

  public static Result broadcastEvent(String type, String id) throws Exception {
    
    ObjectNode json = Json.newObject();
    json.put("type", type);
    
    for (Map.Entry<String, String[]> entry : request().queryString().entrySet()) {
      ArrayNode array = json.arrayNode();
      for (String s : entry.getValue()) {
        array.add(s);
      }
      json.put(entry.getKey(), array);
    }
    
    UIPage page = EntryPoint.getPage();
    System.out.println(page);
    UIComponent component = page.getChildById(id);
    Method method = UIComponent.class.getMethod("processEvent", ObjectNode.class);
    EventNode result = (EventNode) method.invoke(component, json);
    return ok(result);
  }
}
