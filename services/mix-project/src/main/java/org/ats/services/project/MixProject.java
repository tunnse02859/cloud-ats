/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services.project;

import java.util.Date;

import javax.annotation.Nullable;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */

public class MixProject extends BasicDBObject {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  @Inject
  MixProject(@Assisted("_id") String id, @Assisted("name")String name, @Nullable @Assisted("keyword_id") String keywordProjectId, @Nullable @Assisted("performance_id") String performanceProjectId, @Nullable @Assisted("selenium_id") String seleniumProjectId, @Assisted("creator") String creator) {
    
    this.put("_id", id);
    this.put("name", name);
    this.put("keyword_id", keywordProjectId);
    this.put("performance_id", performanceProjectId);
    this.put("selenium_id", seleniumProjectId);
    this.put("created_date", new Date());
    this.put("creator", creator);
  }
  
  public void setId(String id) {
    this.put("_id", id);
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public void setName(String name) {
    this.put("name", name);
  } 
  
  public String getName() {
    return this.getString("name");
  }
  
  public void setPerformanceId(String id) {
    this.put("performance_id", id);
  }
  
  public String getPerformanceId() {
    return this.getString("performance_id");
  }
  
  public void setKeywordId(String id) {
    this.put("keyword_id", id);
  }
  
  public String getKeywordId() {
    return this.getString("keyword_id");
  }
  
  public void setSeleniumId(String id) {
    this.put("selenium_id", id);
  }
  
  public String getSeleniumId() {
    return this.getString("selenium_id");
  }
  
  public void setCreator(String name) {
    this.put("creator", name);
  }
  
  public String getCreator() {
    return this.getString("creator");
  }
}
