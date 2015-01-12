/**
 * 
 */
package models.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import controllers.Application;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class TestProjectModel extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  /** .*/
  private final SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  
  /** .*/
  public static final String PERFORMANCE = "Performance";
  
  /** .*/
  public static final String FUNCTIONAL = "Functional";
  
  public TestProjectModel(Integer gitlabProjectId, String name, String groupId, String userId, String type, String gitSshUrl, String gitHttpUrl, String sourceFile, byte[] sourceContent) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("gitlab_project_id", gitlabProjectId);
    this.put("name", name);
    this.put("group_id", groupId);
    this.put("user_id", userId);
    this.put("type", type);
    this.put("git_ssh_url", gitSshUrl);
    this.put("git_http_url", gitHttpUrl);
    this.put("source_file", sourceFile);
    this.put("source_content", sourceContent);
    this.put("created_date", System.currentTimeMillis());
    this.put("modified_date", System.currentTimeMillis());
  }
   
  public Group getGroupOwner() throws UserManagementException {
    return GroupDAO.getInstance(Application.dbName).findOne(this.getString("group_id"));
  }
  
  public User getCreator() throws UserManagementException {
    return UserDAO.getInstance(Application.dbName).findOne(this.getString("user_id"));
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public String getStatus() {
    return this.getString("status");
  }
  
  public int getGitlabProjectId() {
    return this.getInt("gitlab_project_id");
  }
  
  public String getType() {
    return this.getString("type");
  }
  
  public String getGitSshUrl() {
    return this.getString("git_ssh_url");
  }
  
  public String geGitHttpUrl() {
    return this.getString("git_http_url");
  }
  
  public String getSourceFileName() {
    return this.getString("source_file");
  }
  
  public byte[] getSourceContent() {
    return (byte[]) this.get("source_content");
  }
  
  public String getCreatedDate() {
    Date time = new Date(this.getLong("created_date"));
    return dateFormater.format(time);
  }
  
  public String getModifiedDate() {
    Date time = new Date(this.getLong("modified_date"));
    return dateFormater.format(time);
  }
  
  public String getLastBuildDate() {
    if (this.get("last_build") == null) return "";
    Date time = new Date(this.getLong("last_build"));
    return dateFormater.format(time);
  }
  
  public TestProjectModel() {
    this(0, null, null, null, null, null, null, null, null);
  }
  
  public TestProjectModel from(DBObject source) {
    this.put("_id", source.get("_id"));
    this.put("gitlab_project_id", source.get("gitlab_project_id"));
    this.put("name", source.get("name"));
    this.put("group_id", source.get("group_id"));
    this.put("user_id", source.get("user_id"));
    this.put("type", source.get("type"));
    this.put("git_ssh_url", source.get("git_ssh_url"));
    this.put("git_http_url", source.get("git_http_url"));
    this.put("source_file", source.get("source_file"));
    this.put("source_content", source.get("source_content"));
    this.put("created_date", source.get("created_date"));
    this.put("modified_date", source.get("modified_date"));
    this.put("last_build", source.get("last_build"));
    
    //additional
    this.put("status", source.get("status"));
    this.put("jenkins_id", source.get("jenkins_id"));
    return this;
  }
}
