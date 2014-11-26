/**
 * 
 */
package webui;

import java.util.HashMap;
import java.util.Map;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Nov 25, 2014
 */
public class EventNode extends ObjectNode {

  public EventNode() {
    super(JsonNodeFactory.instance);
  }
  
  public EventNode addEffectedNode(EffectedNode... nodes) {
    JsonNode expected = get("effected");
    if (expected == null) expected = arrayNode();
    for (EffectedNode node : nodes) {
      ((ArrayNode) expected).add(node.toJson());
    }
    put("effected", expected);
    return this;
  }
  
  public EventNode addEvalBlock(EvalNode... nodes) {
    JsonNode eval = get("eval");
    if (eval == null) eval = arrayNode();
    for (EvalNode node : nodes) {
      ((ArrayNode) eval).add(node.toJson());
    }
    put("eval", eval);
    return this;
  }
  
  public EventNode addBroadcastEvent(BroadcastNode... nodes) {
    JsonNode broadcast = get("broadcast");
    if (broadcast == null) broadcast = arrayNode();
    for (BroadcastNode node : nodes) {
      ((ArrayNode) broadcast).add(node.toJson());
    }
    put("broadcast", broadcast);
    return this;
  }
  
  static interface  Node {
    public abstract ObjectNode toJson();
  }

  public static class EffectedNode implements Node {
    
    /** .*/
    public String id;
    
    /** .*/
    public String type;
    
    /** .*/
    public String name;
    
    /** .*/
    public String value;
    
    public EffectedNode(String id, String type, String name, String value) {
      this.id = id;
      this.type = type;
      this.name = name;
      this.value = value;
    }

    @Override
    public ObjectNode toJson() {
      return Json.newObject().put("id", this.id).put("type", type).put("name", name).put("value", value);
    }
  }
  
  public static class EvalNode implements Node {
    /** .*/
    public String eval;
    
    public EvalNode(String eval) {
      this.eval = eval;
    }

    @Override
    public ObjectNode toJson() {
      return Json.newObject().put("eval", eval);
    }
  }
  
  public static class BroadcastNode implements Node {
    
    /** .*/
    public String id;
    
    /** .*/
    public String type;
    
    /** .*/
    private Map<String, String[]> attr = new HashMap<String, String[]>();
    
    public BroadcastNode(String id, String type) {
      this.id = id;
      this.type = type;
    }
    
    public Map<String, String[]> attributes() {
      return attr;
    }

    @Override
    public ObjectNode toJson() {
      ObjectNode json = Json.newObject();
      json.put("id", id);
      json.put("type", type);
      
      ObjectNode attrNode = Json.newObject();
      
      for (Map.Entry<String, String[]> entry : attr.entrySet()) {
        ArrayNode arrayNode = attrNode.arrayNode();
        for (String s : entry.getValue()) {
          arrayNode.add(s);
        }
        attrNode.put(entry.getKey(), arrayNode);
      }
      
      json.put("attr", attrNode);
      return json;
    }
  }
}
