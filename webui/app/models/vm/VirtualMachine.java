/**
 * 
 */
package models.vm;

import java.util.UUID;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 6, 2014
 */
public class VirtualMachine extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  
  public VirtualMachine(String name, String groupId, String publicIp, String username, String password) {
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", name);
    this.put("group_id", groupId);
    this.put("public_ip", publicIp);
    this.put("username", username);
    this.put("password", password);
  }
  
  public VirtualMachine() {
    this(null, null, null, null, null);
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public Group getGroup() {
    try {
      return GroupDAO.INSTANCE.findOne(this.getString("group_id"));
    } catch (UserManagementException e) {
      throw new RuntimeException(e);
    }
  }
  
  public String getPublicIP() {
    return this.getString("public_ip");
  }
  
  public String getUsername() {
    return this.getString("username");
  }
  
  public String getPassword() {
    return this.getString("password");
  }
  
  public VirtualMachine from(DBObject source) {
    this.put("_id", source.get("_id"));
    this.put("name", source.get("name"));
    this.put("group_id", source.get("group_id"));
    this.put("public_ip", source.get("public_ip"));
    this.put("username", source.get("username"));
    this.put("password", source.get("password"));
    return this;
  }
}
