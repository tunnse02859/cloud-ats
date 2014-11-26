package controllers;

import java.util.UUID;

import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;
import webui.UIPage;
import webui.UITextInput;

public class EntryPoint extends Controller {
  
  public static Result index() {
    return ok(getPage().render());
  }
  
  public static UIPage getPage() {
    String sessionId = session("sessionId");
    if (sessionId == null) session().put("sessionId", sessionId = UUID.randomUUID().toString());
    Object obj = Cache.get("UIPage-" + sessionId);
    
    if (obj == null) {
      obj = new UIPage(new UITextInput("name", null), new UITextInput("password", null));
      Cache.set("UIPage-" + sessionId, obj);
    }
    return (UIPage) obj;
  }
  
}
