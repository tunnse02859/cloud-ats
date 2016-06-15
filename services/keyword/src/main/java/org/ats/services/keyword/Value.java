/**
 * 
 */
package org.ats.services.keyword;


/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 9, 2015
 */
@SuppressWarnings("serial")
public class Value extends AbstractTemplate {

  private String value;
  
  private boolean isVariable;
  
  public Value(String value, boolean isVariable) {
    if (value.indexOf("\"") != -1 && !isVariable) value = value.replace("\"", "\\\"");
    this.value = value;
    this.isVariable = isVariable;
  }
  
  public String getValue() {
    return value;
  }
  
  public boolean isVariable() {
    return isVariable;
  }
  
  public String transform() {
    return isVariable ? value : "\"" + value + "\"";
  }
  
  @Override
  public String toString() {
    return this.transform();
  }
}
