/**
 * 
 */
package org.ats.services.functional;

import java.util.logging.Logger;

import org.ats.services.functional.action.AbstractAction;
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
import org.ats.services.functional.locator.AbstractLocator;
import org.ats.services.functional.locator.ClassNameLocator;
import org.ats.services.functional.locator.CssSelectorLocator;
import org.ats.services.functional.locator.IDLocator;
import org.ats.services.functional.locator.LinkTextLocator;
import org.ats.services.functional.locator.NameLocator;
import org.ats.services.functional.locator.PartialLinkTextLocator;
import org.ats.services.functional.locator.TagNameLocator;
import org.ats.services.functional.locator.XPathLocator;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 16, 2015
 */
public class ActionFactory {
  
  @Inject
  private VariableFactory factory;

  @Inject
  private Logger logger;
  
  @SuppressWarnings("serial")
  public AbstractAction createAction(final JsonNode json) {
    if (json == null) return null;
    
    String type = json.get("type").asText();
    
    switch (type) {
    
    case "get":
      Value url = toValue(json.get("url").asText());
      return new Get(url) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "goBack":
      return new GoBack() {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "goForward":
      return new GoForward() {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "clickElement":
      AbstractLocator locator = parseLocator(json.get("locator"));
      return new ClickElement(locator) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "setElementText":
      locator = parseLocator(json.get("locator"));
      Value text = toValue(json.get("text").asText());
      return new SetElementText(locator, text) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "sendKeysToElement":
      locator = parseLocator(json.get("locator"));
      text = toValue(json.get("text").asText());
      return new SendKeysToElement(locator, text) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "doubleClickElement":
      locator = parseLocator(json.get("locator"));
      return new DoubleClickElement(locator) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "mouseOverElement":
      locator = parseLocator(json.get("locator"));
      return new MouseOverElement(locator) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "dragToAndDropElement":
      locator = parseLocator(json.get("locator"));
      AbstractLocator targetLocator = parseLocator(json.get("targetLocator"));
      return new DragToAndDropElement(locator, targetLocator) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "clickAndHoldElement":
      locator = parseLocator(json.get("locator"));
      return new ClickAndHoldElement(locator) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "releaseElement":
      locator = parseLocator(json.get("locator"));
      return new ReleaseElement(locator) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "setElementSelected":
      locator = parseLocator(json.get("locator"));
      return new SetElementSelected(locator) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "clearSelections":
      locator = parseLocator(json.get("locator"));
      return new ClearSelections(locator) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "setElementNotSelected":
      locator = parseLocator(json.get("locator"));
      return new SetElementNotSelected(locator) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "submitElement":
      locator = parseLocator(json.get("locator"));
      return new SubmitElement(locator) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "close":
      return new Close() {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "refresh":
      return new Refresh() {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "assertTextPresent":
      text = toValue(json.get("text").asText());
      boolean negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertTextPresent(text, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "verifyTextPresent":
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyTextPresent(text, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "storeTextPresent":
      String variable = json.get("variable").asText();
      text = toValue(json.get("text").asText());
      return new StoreTextPresent(variable, text, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "assertBodyText":
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertBodyText(text, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyBodyText":
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyBodyText(text, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeBodyText":
      variable = json.get("variable").asText();
      return new StoreBodyText(variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "assertElementPresent":
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertElementPresent(locator, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyElementPresent":
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyElementPresent(locator, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeElementPresent":
      locator = parseLocator(json.get("locator"));
      variable = json.get("variable").asText();
      return new StoreElementPresent(locator, variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertPageSource":
      Value source = toValue(json.get("source").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertPageSource(source, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
     
    case "verifyPageSource":
      source = toValue(json.get("source").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyPageSource(source, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storePageSource":
      variable = json.get("variable").asText();
      return new StorePageSource(variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertText":
      locator = parseLocator(json.get("locator"));
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertText(locator, text, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyText":
      locator = parseLocator(json.get("locator"));
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyText(locator, text, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeText":
      variable = json.get("variable").asText();
      locator = parseLocator(json.get("locator"));
      return new StoreText(variable, locator, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertCurrentUrl":
      url = toValue(json.get("url").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertCurrentUrl(url, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyCurrentUrl":
      url = toValue(json.get("url").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyCurrentUrl(url, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeCurrentUrl":
      variable = json.get("variable").asText();
      return new StoreCurrentUrl(variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertTitle":
      Value title = toValue(json.get("title").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertTitle(title, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyTitle":
      title = toValue(json.get("title").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyTitle(title, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "storeTitle":
      variable = json.get("variable").asText();
      return new StoreTitle(variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertElementAttribute":
      Value value = toValue(json.get("value").asText());
      Value attributeName = toValue(json.get("attributeName").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertElementAttribute(attributeName, value, locator, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyElementAttribute":
      value = toValue(json.get("value").asText());
      attributeName = toValue(json.get("attributeName").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyElementAttribute(locator, attributeName, value, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeElementAttribute":
      variable = json.get("variable").asText();
      attributeName = toValue(json.get("attributeName").asText());
      locator = parseLocator(json.get("locator"));
      return new StoreElementAttribute(variable, attributeName, locator, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertElementStyle":
      Value propertyName = toValue(json.get("propertyName").asText());
      value = toValue(json.get("value").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertElementStyle(propertyName, value, locator, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyElementStyle":
      propertyName = toValue(json.get("propertyName").asText());
      value = toValue(json.get("value").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyElementStyle(propertyName, value, locator, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeElementStyle":
      propertyName = toValue(json.get("propertyName").asText());
      variable = json.get("variable").asText();
      locator = parseLocator(json.get("locator"));
      return new StoreElementStyle(propertyName, variable, locator, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertElementSelected":
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertElementSelected(locator, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyElementSelected":
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyElementSelected(locator, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeElementSelected":
      variable = json.get("variable").asText();
      locator = parseLocator(json.get("locator"));
      return new StoreElementSelected(variable, locator, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertElementValue": 
      value = toValue(json.get("value").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertElementValue(value, locator, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyElementValue":
      value = toValue(json.get("value").asText());
      locator = parseLocator(json.get("locator"));
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyElementValue(locator, value, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeElementValue":
      locator = parseLocator(json.get("locator"));
      variable = json.get("variable").asText();
      return new StoreElementValue(locator, variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "addCookie":
      Value name = toValue(json.get("name").asText());
      value = toValue(json.get("value").asText());
      String options = json.get("options").asText();
      return new AddCookie(name, value, options) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "deleteCookie":
      name = toValue(json.get("name").asText());
      return new DeleteCookie(name) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertCookieByName":
      name = toValue(json.get("name").asText());
      value = toValue(json.get("value").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertCookieByName(name, value, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyCookieByName":
      name = toValue(json.get("name").asText());
      value = toValue(json.get("value").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyCookieByName(name, value, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeCookieByName":
      name = toValue(json.get("name").asText());
      variable = json.get("variable").asText();
      return new StoreCookieByName(name, variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertCookiePresent":
      name = toValue(json.get("name").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertCookiePresent(name, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyCookiePresent":
      name = toValue(json.get("name").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyCookiePresent(name, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeCookiePresent":
      variable = json.get("variable").asText();
      name = toValue(json.get("name").asText());
      return new StoreCookiePresent(variable, name, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "saveScreenshot":
      Value file = toValue(json.get("file").asText());
      return new SaveScreenShot(file) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "print":
      text = toValue(json.get("text").asText());
      return new Print(text) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "store":
      text = toValue(json.get("text").asText());
      variable = json.get("variable").asText();
      return new Store(text, variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "pause":
      long waitTime = json.get("waitTime").asLong();
      return new Pause(waitTime) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "switchToFrame":
      Value identifier = toValue(json.get("identifier").asText());
      return new SwitchToFrame(identifier) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "switchToFrameByIndex":
      int index = json.get("index").asInt();
      return new SwitchToFrameByIndex(index) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "switchToWindow":
      name = toValue(json.get("name").asText());
      return new SwitchToWindow(name) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "switchToDefaultContent":
      return new SwitchToDefaultContent() {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertAlertText":
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertAlertText(text, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyAlertText":
      text = toValue(json.get("text").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyAlertText(text, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "storeAlertText":
      variable = json.get("variable").asText();
      return new StoreAlertText(variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertAlertPresent":
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertAlertPresent(negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyAlertPresent":
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyAlertPresent(negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
    
    case "storeAlertPresent":
      variable = json.get("variable").asText();
      return new StoreAlertPresent(variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "answerAlert":
      text = toValue(json.get("text").asText());
      return new AnswerAlert(text) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "acceptAlert":
      return new AcceptAlert() {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "dismissAlert":
      return new DismissAlert() {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "assertEval":
      Value script = toValue(json.get("script").asText());
      value = toValue(json.get("value").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new AssertEval(script, value, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    case "verifyEval":
      script = toValue(json.get("script").asText());
      value = toValue(json.get("value").asText());
      negated = json.get("negated") == null ? false : json.get("negated").asBoolean();
      return new VerifyEval(script, value, negated) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
   
    case "storeEval":
      script = toValue(json.get("script").asText());
      variable = json.get("variable").asText();
      return new StoreEval(script, variable, factory) {
        @Override
        public DBObject toJson() {
          DBObject dbObj = (DBObject) JSON.parse(json.toString());
          return dbObj;
        }
      };
      
    default:
      logger.warning("Unsupported keyword: " + type);
      return null;
    }
  }
  
  public Value toValue(String str) {
    boolean isVariable = str.startsWith("${") && str.endsWith("}");
    if (isVariable) {
      str = str.substring(2, str.length() - 1);
      return new Value(str, isVariable);
    } else if (str.indexOf("${") != -1 && str.lastIndexOf("}") != -1 ) {
      int start = str.indexOf("${");
      int end = str.indexOf("}");
      String variable = str.substring(start + 2, end);
      StringBuilder sb = new StringBuilder(str.substring(0, start)).append("\" + ");
      sb.append(variable).append(" + \"").append(str.substring(end + 1));
      return toValue(sb.toString());
    }
    return new Value(str, false);
  }
  
  public AbstractLocator parseLocator(JsonNode json) {
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
      logger.warning("Unsupported locator type: " + type);
      return null;
    }
  }
}
