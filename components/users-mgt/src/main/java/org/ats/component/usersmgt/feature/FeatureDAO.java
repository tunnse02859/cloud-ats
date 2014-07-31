/**
 * 
 */
package org.ats.component.usersmgt.feature;

import org.ats.component.usersmgt.ManagementDAO;
import org.ats.component.usersmgt.UserManagementException;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jul 9, 2014
 */
public class FeatureDAO extends ManagementDAO<Feature> {
  
  public static final FeatureDAO INSTANCE = new FeatureDAO();
  
  private FeatureDAO() {
    super("feature");
  }
  
  @Override
  public Feature transform(DBObject obj) throws UserManagementException {
    return obj == null ? null : new Feature(obj);
  }

}
