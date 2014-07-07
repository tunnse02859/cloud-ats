/**
 * 
 */
package org.ats.cloudstack;

import java.util.List;

import junit.framework.Assert;

import org.ats.cloudstack.model.SecurityGroup;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class SecurityGrouAPITestCase {

  @Test
  public void listSecurityGroups() throws Exception {
    List<SecurityGroup> list1 = SecurityGroupAPI.listSecurityGroups(null, null, null, null);
    Assert.assertEquals(1, list1.size());
    
    List<SecurityGroup> list2 = SecurityGroupAPI.listSecurityGroups(null, null, null, "default");
    Assert.assertEquals(1, list2.size());

    SecurityGroup s1 = list1.get(0);
    SecurityGroup s2 = list2.get(0);
    Assert.assertEquals(s1.id, s2.id);
    Assert.assertEquals(s1.name, s2.name);
    Assert.assertEquals(s1.domainId, s2.domainId);
  }
}
