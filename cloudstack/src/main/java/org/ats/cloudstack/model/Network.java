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
public class Network extends AbstractModel {

  /** .*/
  public String id;
  
  /** .*/
  public String name;
  
  /** .*/
  @JsonProperty("displaytext")
  public String displayText;
  
  /** .*/
  @JsonProperty("zoneid")
  public String zoneId;
  
  /** .*/
  @JsonProperty("zoneName")
  public String zoneName;
}
