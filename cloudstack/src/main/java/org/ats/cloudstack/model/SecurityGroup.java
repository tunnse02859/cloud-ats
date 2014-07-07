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
public class SecurityGroup extends AbstractModel {

  /** .*/
  public String id;
  
  /** .*/
  public String name;
  
  /** .*/
  public String description;
  
  /** .*/
  public String account;
  
  /** .*/
  @JsonProperty("domainid")
  public String domainId;
  
  /** .*/
  public String domain;
}
