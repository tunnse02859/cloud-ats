/**
 * 
 */
package org.ats.services.organization.entities;

import org.ats.services.data.common.Reference;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class Space extends BasicDBObject {

  /** .*/
  private static final long serialVersionUID = 1L;

  public static class SpaceRef extends Reference<Space> {

    public SpaceRef(String id) {
      super(id);
    }

    @Override
    public Space getInstance() {
      return null;
    }
    
  }
}
