/**
 * 
 */
package org.ats.services.functional;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 9, 2015
 */
public class Value {

  private String value;
  
  private boolean isVariable;
  
  public Value(String value, boolean isVariable) {
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
