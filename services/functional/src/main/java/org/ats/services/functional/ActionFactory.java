/**
 * 
 */
package org.ats.services.functional;

import org.ats.services.functional.action.AcceptAlert;
import org.ats.services.functional.action.AddCookie;
import org.ats.services.functional.action.AnswerAlert;
import org.ats.services.functional.action.AssertAlertPresent;
import org.ats.services.functional.action.AssertAlertText;
import org.ats.services.functional.action.AssertBodyText;
import org.ats.services.functional.action.AssertCookieByName;
import org.ats.services.functional.action.AssertCookiePresent;
import org.ats.services.functional.action.AssertCurrentUrl;
import org.ats.services.functional.action.AssertElementAttribute;
import org.ats.services.functional.action.AssertElementPresent;
import org.ats.services.functional.action.AssertElementSelected;
import org.ats.services.functional.action.AssertElementStyle;
import org.ats.services.functional.action.AssertElementValue;
import org.ats.services.functional.action.AssertEval;
import org.ats.services.functional.action.AssertPageSource;
import org.ats.services.functional.action.AssertText;
import org.ats.services.functional.action.AssertTextPresent;
import org.ats.services.functional.action.AssertTitle;
import org.ats.services.functional.action.ClearSelections;
import org.ats.services.functional.action.ClickAndHoldElement;
import org.ats.services.functional.action.ClickElement;
import org.ats.services.functional.action.Close;
import org.ats.services.functional.action.DeleteCookie;
import org.ats.services.functional.action.DismissAlert;
import org.ats.services.functional.action.DoubleClickElement;
import org.ats.services.functional.action.DragToAndDropElement;
import org.ats.services.functional.action.Get;
import org.ats.services.functional.action.GoBack;
import org.ats.services.functional.action.GoForward;
import org.ats.services.functional.action.IAction;
import org.ats.services.functional.action.MouseOverElement;
import org.ats.services.functional.action.Pause;
import org.ats.services.functional.action.Print;
import org.ats.services.functional.action.Refresh;
import org.ats.services.functional.action.ReleaseElement;
import org.ats.services.functional.action.SaveScreenShot;
import org.ats.services.functional.action.SendKeysToElement;
import org.ats.services.functional.action.SetElementNotSelected;
import org.ats.services.functional.action.SetElementSelected;
import org.ats.services.functional.action.SetElementText;
import org.ats.services.functional.action.Store;
import org.ats.services.functional.action.StoreAlertPresent;
import org.ats.services.functional.action.StoreAlertText;
import org.ats.services.functional.action.StoreBodyText;
import org.ats.services.functional.action.StoreCookieByName;
import org.ats.services.functional.action.StoreCookiePresent;
import org.ats.services.functional.action.StoreCurrentUrl;
import org.ats.services.functional.action.StoreElementAttribute;
import org.ats.services.functional.action.StoreElementPresent;
import org.ats.services.functional.action.StoreElementSelected;
import org.ats.services.functional.action.StoreElementStyle;
import org.ats.services.functional.action.StoreElementValue;
import org.ats.services.functional.action.StoreEval;
import org.ats.services.functional.action.StorePageSource;
import org.ats.services.functional.action.StoreText;
import org.ats.services.functional.action.StoreTextPresent;
import org.ats.services.functional.action.StoreTitle;
import org.ats.services.functional.action.SubmitElement;
import org.ats.services.functional.action.SwitchToDefaultContent;
import org.ats.services.functional.action.SwitchToFrame;
import org.ats.services.functional.action.SwitchToFrameByIndex;
import org.ats.services.functional.action.SwitchToWindow;
import org.ats.services.functional.action.VerifyAlertPresent;
import org.ats.services.functional.action.VerifyAlertText;
import org.ats.services.functional.action.VerifyBodyText;
import org.ats.services.functional.action.VerifyCookieByName;
import org.ats.services.functional.action.VerifyCookiePresent;
import org.ats.services.functional.action.VerifyCurrentUrl;
import org.ats.services.functional.action.VerifyElementAttribute;
import org.ats.services.functional.action.VerifyElementPresent;
import org.ats.services.functional.action.VerifyElementSelected;
import org.ats.services.functional.action.VerifyElementStyle;
import org.ats.services.functional.action.VerifyElementValue;
import org.ats.services.functional.action.VerifyEval;
import org.ats.services.functional.action.VerifyPageSource;
import org.ats.services.functional.action.VerifyText;
import org.ats.services.functional.action.VerifyTextPresent;
import org.ats.services.functional.action.VerifyTitle;
import org.ats.services.functional.locator.ClassNameLocator;
import org.ats.services.functional.locator.CssSelectorLocator;
import org.ats.services.functional.locator.IDLocator;
import org.ats.services.functional.locator.ILocator;
import org.ats.services.functional.locator.LinkTextLocator;
import org.ats.services.functional.locator.NameLocator;
import org.ats.services.functional.locator.PartialLinkTextLocator;
import org.ats.services.functional.locator.TagNameLocator;
import org.ats.services.functional.locator.XPathLocator;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 16, 2015
 */
public class ActionFactory {
  
  private VariableFactory factory;
  
  @Inject
  public ActionFactory(VariableFactory factory) {
    this.factory = factory;
  }

  public IAction createAction(JsonNode json) {
    if (json == null) return null;
    
    String type = json.get("type").asText();
    
    switch (type) {
    
    case "get":
      Value url = toValue(json.get("url").asText());
      return new Get(url);
    
    case "goBack":
      return new GoBack();
    
    case "goForward":
      return new GoForward();
    
    case "clickElement":
      ILocator locator = parseLocator(json.get("locator"));
      return new ClickElement(locator);
    
    case "setElementText":
      locator = parseLocator(json.get("locator"));
      Value text = toValue(json.get("text").asText());
      return new SetElementText(locator, text);
    
    case "sendKeysToElement":
      locator = parseLocator(json.get("locator"));
      text = toValue(json.get("text").asText());
      return new SendKeysToElement(locator, text);
    
    case "doubleClickElement":
      locator = parseLocator(json.get("locator"));
      return new DoubleClickElement(locator);
    
    case "mouseOverElement":
      locator = parseLocator(json.get("locator"));
      return new MouseOverElement(locator);
    
    case "dragToAndDropElement":
      locator = parseLocator(json.get("locator"));
      ILocator targetLocator = parseLocator(json.get("targetLocator"));
      return new DragToAndDropElement(locator, targetLocator);
    
    case "clickAndHoldElement":
      locator = parseLocator(json.get("locator"));
      return new ClickAndHoldElement(locator);
    
    case "releaseElement":
      locator = parseLocator(json.get("locator"));
      return new ReleaseElement(locator);
    
    case "setElementSelected":
      locator = parseLocator(json.get("locator"));
      return new SetElementSelected(locator);
    
    case "clearSelections":
      locator = parseLocator(json.get("locator"));
      return new ClearSelections(locator);
    
    case "setElementNotSelected":
      locator = parseLocator(json.get("locator"));
      return new SetElementNotSelected(locator);
    
    case "submitElement":
      locator = parseLocator(json.get("locator"));
      return new SubmitElement(locator);
    
    case "close":
      return new Close();
    
    case "refresh":
      return new Refresh();
    
    case "assertTextPresent":
      text = toValue(json.get("text").asText());
      boolean negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertTextPresent(text, negated);
    
    case "verifyTextPresent":
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyTextPresent(text, negated);
    
    case "storeTextPresent":
      String variable = json.get("variable").asText();
      text = toValue(json.get("text").asText());
      return new StoreTextPresent(variable, text, factory);
    
    case "assertBodyText":
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertBodyText(text, negated);
      
    case "verifyBodyText":
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyBodyText(text, negated);
      
    case "storeBodyText":
      variable = json.get("variable").asText();
      return new StoreBodyText(variable, factory);
    
    case "assertElementPresent":
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertElementPresent(locator, negated);
      
    case "verifyElementPresent":
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyElementPresent(locator, negated);
      
    case "storeElementPresent":
      locator = parseLocator(json.get("locator"));
      variable = json.get("variable").asText();
      return new StoreElementPresent(locator, variable, factory);
      
    case "assertPageSource":
      Value source = toValue(json.get("source").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertPageSource(source, negated);
     
    case "verifyPageSource":
      source = toValue(json.get("source").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyPageSource(source, negated);
      
    case "storePageSource":
      variable = json.get("variable").asText();
      return new StorePageSource(variable, factory);
      
    case "assertText":
      locator = parseLocator(json.get("locator"));
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertText(locator, text, negated);
      
    case "verifyText":
      locator = parseLocator(json.get("locator"));
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyText(locator, text, negated);
      
    case "storeText":
      variable = json.get("variable").asText();
      locator = parseLocator(json.get("locator"));
      return new StoreText(variable, locator, factory);
      
    case "assertCurrentUrl":
      url = toValue(json.get("url").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertCurrentUrl(url, negated);
      
    case "verifyCurrentUrl":
      url = toValue(json.get("url").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyCurrentUrl(url, negated);
      
    case "storeCurrentUrl":
      variable = json.get("variable").asText();
      return new StoreCurrentUrl(variable, factory);
      
    case "assertTitle":
      Value title = toValue(json.get("title").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertTitle(title, negated);
      
    case "verifyTitle":
      title = toValue(json.get("title").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyTitle(title, negated);
    
    case "storeTitle":
      variable = json.get("variable").asText();
      return new StoreTitle(variable, factory);
      
    case "assertElementAttribute":
      Value value = toValue(json.get("value").asText());
      Value attributeName = toValue(json.get("attributeName").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertElementAttribute(attributeName, value, locator, negated);
      
    case "verifyElementAttribute":
      value = toValue(json.get("value").asText());
      attributeName = toValue(json.get("attributeName").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyElementAttribute(locator, attributeName, value, negated);
      
    case "storeElementAttribute":
      variable = json.get("variable").asText();
      attributeName = toValue(json.get("attributeName").asText());
      locator = parseLocator(json.get("locator"));
      return new StoreElementAttribute(variable, attributeName, locator, factory);
      
    case "assertElementStyle":
      Value propertyName = toValue(json.get("propertyName").asText());
      value = toValue(json.get("value").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertElementStyle(propertyName, value, locator, negated);
      
    case "verifyElementStyle":
      propertyName = toValue(json.get("propertyName").asText());
      value = toValue(json.get("value").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyElementStyle(propertyName, value, locator, negated);
      
    case "storeElementStyle":
      propertyName = toValue(json.get("propertyName").asText());
      variable = json.get("variable").asText();
      locator = parseLocator(json.get("locator"));
      return new StoreElementStyle(propertyName, variable, locator, factory);
      
    case "assertElementSelected":
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertElementSelected(locator, negated);
      
    case "verifyElementSelected":
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyElementSelected(locator, negated);
      
    case "storeElementSelected":
      variable = json.get("variable").asText();
      locator = parseLocator(json.get("locator"));
      return new StoreElementSelected(variable, locator, factory);
      
    case "assertElementValue": 
      value = toValue(json.get("value").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertElementValue(value, locator, negated);
      
    case "verifyElementValue":
      value = toValue(json.get("value").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyElementValue(locator, value, negated);
      
    case "storeElementValue":
      locator = parseLocator(json.get("locator"));
      variable = json.get("variable").asText();
      return new StoreElementValue(locator, variable, factory);
      
    case "addCookie":
      Value name = toValue(json.get("name").asText());
      value = toValue(json.get("value").asText());
      String options = json.get("options").asText();
      return new AddCookie(name, value, options);
    
    case "deleteCookie":
      name = toValue(json.get("name").asText());
      return new DeleteCookie(name);
      
    case "assertCookieByName":
      name = toValue(json.get("name").asText());
      value = toValue(json.get("value").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertCookieByName(name, value, negated);
      
    case "verifyCookieByName":
      name = toValue(json.get("name").asText());
      value = toValue(json.get("value").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyCookieByName(name, value, negated);
      
    case "storeCookieByName":
      name = toValue(json.get("name").asText());
      variable = json.get("variable").asText();
      return new StoreCookieByName(name, variable, factory);
      
    case "assertCookiePresent":
      name = toValue(json.get("name").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertCookiePresent(name, negated);
      
    case "verifyCookiePresent":
      name = toValue(json.get("name").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyCookiePresent(name, negated);
      
    case "storeCookiePresent":
      variable = json.get("variable").asText();
      name = toValue(json.get("name").asText());
      return new StoreCookiePresent(variable, name, factory);
      
    case "saveScreenshot":
      Value file = toValue(json.get("file").asText());
      return new SaveScreenShot(file);
      
    case "print":
      text = toValue(json.get("text").asText());
      return new Print(text);
      
    case "store":
      text = toValue(json.get("text").asText());
      variable = json.get("variable").asText();
      return new Store(text, variable, factory);
      
    case "pause":
      long waitTime = json.get("waitTime").asLong();
      return new Pause(waitTime);
      
    case "switchToFrame":
      Value identifier = toValue(json.get("identifier").asText());
      return new SwitchToFrame(identifier);
      
    case "switchToFrameByIndex":
      int index = json.get("index").asInt();
      return new SwitchToFrameByIndex(index);
      
    case "switchToWindow":
      name = toValue(json.get("name").asText());
      return new SwitchToWindow(name);
      
    case "switchToDefaultContent":
      return new SwitchToDefaultContent();
      
    case "assertAlertText":
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertAlertText(text, negated);
      
    case "verifyAlertText":
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyAlertText(text, negated);
      
    case "storeAlertText":
      variable = json.get("variable").asText();
      return new StoreAlertText(variable, factory);
      
    case "assertAlertPresent":
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertAlertPresent(negated);
      
    case "verifyAlertPresent":
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyAlertPresent(negated);
    
    case "storeAlertPresent":
      variable = json.get("variable").asText();
      return new StoreAlertPresent(variable, factory);
      
    case "answerAlert":
      text = toValue(json.get("text").asText());
      return new AnswerAlert(text);
      
    case "acceptAlert":
      return new AcceptAlert();
      
    case "dismissAlert":
      return new DismissAlert();
      
    case "assertEval":
      Value script = toValue(json.get("script").asText());
      value = toValue(json.get("value").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertEval(script, value, negated);
      
    case "verifyEval":
      script = toValue(json.get("script").asText());
      value = toValue(json.get("value").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyEval(script, value, negated);
   
    case "storeEval":
      script = toValue(json.get("script").asText());
      variable = json.get("variable").asText();
      return new StoreEval(script, variable, factory);
      
    default:
      return null;
    }
  }
  
  public Value toValue(String str) {
    boolean isVariable = str.startsWith("${") && str.endsWith("}");
    if (isVariable) {
      str = str.substring(2, str.length() - 1);
    }
    return new Value(str, isVariable);
  }
  
  public ILocator parseLocator(JsonNode json) {
    String type = json.get("type").asText();
    Value locator = toValue(json.get("value").asText());
    
    switch (type) {
    
    case "class name":
      return new ClassNameLocator(locator);
    case "id":
      return new IDLocator(locator);
    case "link text":
      return new LinkTextLocator(locator);
    case "xpath":
      return new XPathLocator(locator);
    case "css selector":
      return new CssSelectorLocator(locator);
    case "name":
      return new NameLocator(locator);
    case "tag name":
      return new TagNameLocator(locator);
    case "partial link text":
      return new PartialLinkTextLocator(locator);
    default:
      return null;
    }
  }
}
