/**
 * 
 */
package org.ats.cloudstack.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 26, 2014
 */
public class Volume extends AbstractModel {

  /** .*/
  public String id;
  
  /** .*/
  public String name;
  
  /** .*/
  @JsonProperty("zoneId")
  public String zoneId;
  
  /** .*/
  @JsonProperty("zonename")
  public String zoneName;
  
  /** .*/
  public String type;
  
  /** .*/
  public String state;
  
  /** .*/
  @JsonProperty("diskofferingid")
  public String diskOfferingId;
  
  /** .*/
  @JsonProperty("diskofferingname")
  public String diskOfferingName;
  
  /** .*/
  @JsonProperty("virtualmachineid")
  public String vmId;
  
  /** .*/
  @JsonProperty("vmname")
  public String vmName;
  
  /** .*/
  @JsonProperty("vmstate")
  public String vmState;
}
