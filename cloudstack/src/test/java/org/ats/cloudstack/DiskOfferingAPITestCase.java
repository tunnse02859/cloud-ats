/**
 * 
 */
package org.ats.cloudstack;

import java.util.List;

import junit.framework.Assert;

import org.ats.cloudstack.model.DiskOffering;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class DiskOfferingAPITestCase {

  @Test
  public void listDiskOfferings() throws Exception {
    List<DiskOffering> list = DiskOfferingAPI.listDiskOfferings(null, null);
    Assert.assertEquals(5, list.size());
    
    list = DiskOfferingAPI.listDiskOfferings(null, "Medium");
    Assert.assertEquals(1, list.size());
    
    DiskOffering dof = list.get(0);
    Assert.assertEquals("Medium", dof.name);
    Assert.assertEquals(20, dof.diskSize);
    Assert.assertEquals("Medium Disk, 20 GB", dof.displayText);
  }
}
