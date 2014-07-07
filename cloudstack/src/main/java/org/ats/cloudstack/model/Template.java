/**
 * 
 */
package org.ats.cloudstack.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 24, 2014
 */
public class Template extends AbstractModel {

  /** .*/
  public String id;
  
  /** .*/
  public String name;
  
  /** .*/
  @JsonProperty("displayname")
  public String displayName;
  
  /** .*/
  @JsonProperty("ispublic")
  public boolean isPublic;
  
  /** .*/
  @JsonProperty("isfeatured")
  public boolean isFeatured;
  
  /** .*/
  @JsonProperty("isready")
  public boolean isReady;
  
  /** .*/
  @JsonProperty("ostypeid")
  public String osTypeId;
  
  /** .*/
  @JsonProperty("ostypename")
  public String osTypeName;
  
  /** .*/
  @JsonProperty("zoneid")
  public String zoneId;
  
  /** .*/
  @JsonProperty("zonename")
  public String zoneName;
  
  /** .*/
  @JsonProperty("sourcetemplateid")
  public String sourceTemplateId;
}
