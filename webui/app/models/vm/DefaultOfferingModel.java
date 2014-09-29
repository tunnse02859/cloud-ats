/**
 * 
 */
package models.vm;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;

import utils.OfferingHelper;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 24, 2014
 */
public class DefaultOfferingModel extends BasicDBObject {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public DefaultOfferingModel(String groupId, String offeringId) {
    this.put("_id", groupId);
    this.put("offering_id", offeringId);
  }
  
  public DefaultOfferingModel() {
    this(null, null);
  }
  
  public Group getGroup() throws UserManagementException {
    return GroupDAO.INSTANCE.findOne(this.getString("_id"));
  }
  
  public OfferingModel getOffering() {
    return OfferingHelper.getOffering(this.getString("offering_id"));
  }
  
  public DefaultOfferingModel from(DBObject source) {
    this.put("_id", source.get("_id"));
    this.put("offering_id", source.get("offering_id"));
    return this;
  }
}
