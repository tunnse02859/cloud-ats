/**
 * 
 */
package org.ats.services.organization;

import org.ats.services.organization.entities.Tenant;

import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 10, 2015
 */
public class TenantService extends AbstractMongoCRUD<Tenant> {

  public Tenant transform(DBObject source) {
    return null;
  }
}
