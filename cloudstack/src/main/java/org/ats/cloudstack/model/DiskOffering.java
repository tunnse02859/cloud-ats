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
public class DiskOffering extends AbstractModel {

  /** .*/
  public String id;
  
  /** .*/
  public String name;
  
  /** .*/
  @JsonProperty("displaytext")
  public String displayText;
  
  /** .*/
  @JsonProperty("disksize")
  public int diskSize;
}
