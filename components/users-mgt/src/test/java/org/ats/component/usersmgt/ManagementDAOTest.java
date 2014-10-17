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
  
  /** .*/
  private final String dbName = "cloud-ats-test";

  @Test
  public void testIndexSearch() throws UserManagementException {
    Group g1 = new Group(dbName, "System Admin");
    Group g2 = new Group(dbName, "Group Admin");
    GroupDAO.getInstance(dbName).create(g1, g2);

    System.out.println(GroupDAO.getInstance(dbName).getColumn().getIndexInfo());
    
    BasicDBObject query = new BasicDBObject();
    query.put("$text", new BasicDBObject("$search", "admin"));
    Collection<Group> groups = GroupDAO.getInstance(dbName).find(query);
    
    Assert.assertEquals(2, groups.size());
    Assert.assertTrue(groups.contains(g1));
    Assert.assertTrue(groups.contains(g2));
    
    g1.put("name", "System");
    GroupDAO.getInstance(dbName).update(g1);
    
    groups = GroupDAO.getInstance(dbName).find(query);
    Assert.assertEquals(1, groups.size());
    Assert.assertEquals("Group Admin", groups.iterator().next().get("name"));
  }
  
  @After
  public void tearDown() {
    DataFactory.dropDatabase(dbName);
  }
}
