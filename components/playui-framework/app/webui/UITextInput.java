/**
 * 
 */
package webui;

import play.twirl.api.Html;
import webui.EventNode.BroadcastNode;
import webui.EventNode.EffectedNode;
import webui.EventNode.EvalNode;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Nov 24, 2014
 */
public class UITextInput extends UIComponent {

  /** The name of input. */
  private String name;
  
  /** The value of input. */
  private String value;
  
  public UITextInput() {
    super();
  }
  
  public UITextInput(String name, String value) {
    super();
    this.name = name;
    this.value = value;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public Html render() {
    return webui.html.uitextinput.render(name, value, null, getId(), attributes());
  }

  @Override
  public EventNode processEvent(ObjectNode event) {
    String type = event.get("type").asText();
    
    if ("KeyEnter".equals(type)) {
      return processKeyEnterEvent(event);
    }
    return null;
  }
  
  private EventNode processKeyEnterEvent(ObjectNode event) {
    
    EventNode node = new EventNode();
    
    String value = event.get("value") != null ? event.get("value").get(0).asText() : event.get("value[]").get(0).asText();
    this.setValue(value + " Affected");
    
    EvalNode evalNode = new EvalNode("$('#" + getId() + "').val('" + getValue() + "')");
    node.addEvalBlock(evalNode);
    
    if (next() != null) {
      BroadcastNode broadcastNode = new BroadcastNode(next().getId(), "KeyEnter");
      broadcastNode.attributes().put("value", new String[] { value });
      node.addBroadcastEvent(broadcastNode);
    }
    
    this.addAttribute("disabled", "disabled");
    node.addEffectedNode(new EffectedNode(getId(), "attr", "disabled", "disabled"));
    return node;
  }
}
