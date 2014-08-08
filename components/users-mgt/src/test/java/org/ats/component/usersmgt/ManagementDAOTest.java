/**
 * 
 */
package org.ats.component.usersmgt;


import java.util.Collection;

import junit.framework.Assert;

import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.junit.After;
import org.junit.Test;

import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 8, 2014
 */
public class ManagementDAOTest {

  @Test
  public void testIndexSearch() throws UserManagementException {
    Group g1 = new Group("System Admin");
    Group g2 = new Group("Group Admin");
    GroupDAO.INSTANCE.create(g1, g2);

    System.out.println(GroupDAO.INSTANCE.getColumn().getIndexInfo());
    
    BasicDBObject query = new BasicDBObject();
    query.put("$text", new BasicDBObject("$search", "admin"));
    Collection<Group> groups = GroupDAO.INSTANCE.find(query);
    
    Assert.assertEquals(2, groups.size());
    Assert.assertTrue(groups.contains(g1));
    Assert.assertTrue(groups.contains(g2));
  }
  
  @After
  public void tearDown() {
    DataFactory.dropDatabase("cloud-ats");
  }
}
