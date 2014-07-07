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
public class VirtualMachine extends AbstractModel {
  
  /** .*/
  public String id;
  
  /** .*/
  public String name;
  
  /** .*/
  @JsonProperty("displayname")
  public String displayName;
  
  /** .*/
  public String state;
  
  /** .*/
  @JsonProperty("templateid")
  public String templateId;
  
  /** .*/
  @JsonProperty("templatename")
  public String templateName;
  
  /** .*/
  public Nic[] nic;
  
  public static class Nic {
    
    /** .*/
    public String id;
    
    /** .*/
    @JsonProperty("ipaddress")
    public String ipAddress;
  }
}
