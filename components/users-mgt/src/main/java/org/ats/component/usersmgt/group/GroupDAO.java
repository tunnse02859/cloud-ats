/**
 * 
 */
package org.ats.component.usersmgt.group;

import org.ats.component.usersmgt.ManagementDAO;
import org.ats.component.usersmgt.UserManagementException;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 9, 2014
 */
public class GroupDAO extends ManagementDAO<Group> {

  public static final GroupDAO INSTANCE = new GroupDAO();
  
  private GroupDAO() {
    super("group");
  }
  
  @Override
  public Group transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new Group(obj);
  }

}
