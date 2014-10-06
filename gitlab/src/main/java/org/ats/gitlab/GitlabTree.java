/**
 * 
 */
package org.ats.gitlab;



/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 2, 2014
 */
public class GitlabTree {
  
  public static String URL = "/repository/tree";
  
  /** .*/
  private String id;
  
  /** .*/
  private String name;
  
  /** .*/
  private  String type;
  
  /** .*/
  private String mode;
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }
  
}
