/**
 * 
 */
package org.ats.component.usersmgt.feature;

import java.util.Collection;
import java.util.regex.Pattern;

import org.ats.component.usersmgt.Event;
import org.ats.component.usersmgt.EventExecutedException;
import org.ats.component.usersmgt.EventListener;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.PermissionDAO;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 15, 2014
 */
public class OperationEventListener implements EventListener {
  
  public void execute(Event event) throws EventExecutedException {
    if ("delete-operation".equals(event.getType())) {
      try {
        this.processDeleteOperationInFeature(event);
        this.processDeleteOperationInPermission(event);
      } catch (UserManagementException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    }
  }
  
  /**
   * Delete operation_id in Feature
   * @throws UserManagementException 
   */
  private void processDeleteOperationInFeature(Event event) throws UserManagementException {
    Operation op = new Operation(event.getSource());
    Pattern p = Pattern.compile(op.getId());
    
    Collection<Feature> features = FeatureDAO.INSTANCE.find(new BasicDBObject("operation_ids", p));
    for (Feature f : features) {
      f.removeOperation(op);
      FeatureDAO.INSTANCE.update(f);
    }
  }
  
  private void processDeleteOperationInPermission(Event event) throws UserManagementException {
    Operation op = new Operation(event.getSource());
    Collection<Permission> perms = PermissionDAO.INSTANCE.find(new BasicDBObject("operation_id", op.getId()));
    for (Permission perm : perms) {
      PermissionDAO.INSTANCE.delete(perm);
    }
  }
  
}
