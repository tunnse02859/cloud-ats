/**
 * 
 */
package models.test;

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
  
  public TestProjectModel(String name, String groupId, String userId, TestProjectType type, String gitUrl, String sourceFile, byte[] sourceContent) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", name);
    this.put("group_id", groupId);
    this.put("user_id", userId);
    this.put("type", type.toString());
    this.put("git_url", gitUrl);
    this.put("source_file", sourceFile);
    this.put("source_content", sourceContent);
  }
  
  public Group getGroupOwner() throws UserManagementException {
    return GroupDAO.getInstance(Application.dbName).findOne(this.getString("group_id"));
  }
  
  public User getCreator() throws UserManagementException {
    return UserDAO.getInstance(Application.dbName).findOne(this.getString("user_id"));
  }
  
  public TestProjectModel getType() {
    return (TestProjectModel) this.get("type");
  }
  
  public String getGitUrl() {
    return this.getString("git_url");
  }
  
  public String getSourceFileName() {
    return this.getString("source_file");
  }
  
  public byte[] getSourceContent() {
    return (byte[]) this.get("source_content");
  }
  
  public TestProjectModel() {
    this(null, null, null, null, null, null, null);
  }
  
  public TestProjectModel from(DBObject source) {
    this.put("_id", source.get("_id"));
    this.put("name", source.get("name"));
    this.put("group_id", source.get("group_id"));
    this.put("user_id", source.get("user_id"));
    this.put("type", source.get("type"));
    this.put("git_url", source.get("git_url"));
    this.put("source_file", source.get("source_file"));
    this.put("source_content", source.get("source_content"));
    
    //additional
    this.put("jmeter", source.get("jmeter"));
    return this;
  }

  public static enum TestProjectType {
    PERFORMANCE, FUNCTIONAL;
  }
}
