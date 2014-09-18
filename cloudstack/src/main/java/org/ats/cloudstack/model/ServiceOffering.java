/**
 * 
 */
package org.ats.cloudstack.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class ServiceOffering extends AbstractModel {

  /** .*/
  public String id;
  
  /** .*/
  public String name;
  
  /** .*/
  @JsonProperty("displaytext")
  public String displayText;
  
  /** .*/
  @JsonProperty("cpunumber")
  public int cpuNumber;
  
  /** .*/
  @JsonProperty("cpuspeed")
  public int cpuSpeed;
  
  /** .*/
  public int memory;
}
