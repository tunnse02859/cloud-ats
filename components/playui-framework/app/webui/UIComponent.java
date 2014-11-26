/**
 * 
 */
package webui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import play.twirl.api.Html;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Nov 24, 2014
 */
public abstract class UIComponent {
  
  /** .*/
  private LinkedList<UIComponent> children;
  
  /** .*/
  private UIComponent parent;
  
  /** .*/
  private UIComponent previous;
  
  /** .*/
  private UIComponent next;
  
  /** .*/
  private String id;
  
  /** .*/
  private Map<String, String> attributes;
  
  public UIComponent() {
    this.id = UUID.randomUUID().toString();
  }
  
  public UIComponent(String id) {
    this.id = id;
  }
  
  public UIComponent getChildById(String id) {
    if (this.id.equals(id)) return this;
    if (!hasChildren()) return null;
    UIComponent found = null;
    for (UIComponent child : this.children) {
       found = child.getChildById(id);
       if (found != null) return found;
    }
    return found;
  }
  
  public List<UIComponent> getChildrenByClass(Class<? extends UIComponent> clazz) {
    List<UIComponent> list = new ArrayList<UIComponent>();
    for (UIComponent child : children()) {
      if (child.getClass().isAssignableFrom(clazz)) list.add(child);
    }
    return list;
  }
  
  public UIComponent parent() {
    return parent;
  }
  
  public UIComponent next() {
    return next;
  }
  
  public UIComponent previous() {
    return previous;
  }
  
  public boolean hasChildren() {
    return children == null ? false : ! children.isEmpty();
  }
  
  public UIComponent addChild(UIComponent child) {
    if (child == null) return this;
    if (children == null) children = new LinkedList<UIComponent>();
    
    child.parent = this;
    
    UIComponent lastest = children.peekLast();
    if (lastest != null) lastest.next = child;
    child.previous = lastest;
    
    children.add(child);
    return this;
  }
  
  public UIComponent removeChild(UIComponent child) {
    if (child == null) return this;
    children.remove(child);
    
    UIComponent previous = child.previous;
    if (previous != null) previous.next = null;
    
    UIComponent next  = child.next;
    if (next != null) next.previous = null;
    
    return this;
  }
  
  public List<UIComponent> children() {
    return children == null ? Collections.<UIComponent>emptyList() : Collections.unmodifiableList(children);
  }
  
  public boolean hasAttribute() {
    return attributes == null ? false : ! attributes.isEmpty();
  }
  
  public UIComponent addAttribute(String name, String value) {
    if (attributes == null) attributes = new HashMap<String, String>();
    attributes.put(name, value);
    return this;
  }
  
  public Map<String, String> attributes() {
    return attributes == null ? Collections.<String, String>emptyMap() : Collections.unmodifiableMap(attributes);
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  public abstract Html render();
  
  public abstract EventNode processEvent(ObjectNode event);
}
