/**
 * 
 */
package webui;

import play.twirl.api.Html;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Nov 24, 2014
 */
public class UIPage extends UIComponent {

  public UIPage(UIComponent... children) {
    for (UIComponent child : children) {
      addChild(child);
    }
  }
  
  @Override
  public Html render() {
    return webui.html.uipage.render(children());
  }

  @Override
  public EventNode processEvent(ObjectNode event) {
    return null;
  }
}
