/**
 * 
 */
package org.ats.cloudstack;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.ats.cloudstack.model.DiskOffering;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class DiskOfferingAPITestCase {
  
 protected CloudStackClient client;
  
  @BeforeClass
  public void setUp() throws Exception {
    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("cs.properties");
    Properties csProps = new Properties();
    csProps.load(is);
    String host = csProps.getProperty("host");
    String apiKey = csProps.getProperty("api-key");
    String secretKey = csProps.getProperty("secret-key");
    this.client = new CloudStackClient(host, apiKey, secretKey);
  }

  @Test
  public void listDiskOfferings() throws Exception {
    List<DiskOffering> list = DiskOfferingAPI.listDiskOfferings(client, null, null);
    Assert.assertEquals(5, list.size());
    
    list = DiskOfferingAPI.listDiskOfferings(client, null, "Medium");
    Assert.assertEquals(1, list.size());
    
    DiskOffering dof = list.get(0);
    Assert.assertEquals("Medium", dof.name);
    Assert.assertEquals(20, dof.diskSize);
    Assert.assertEquals("Medium Disk, 20 GB", dof.displayText);
  }
}
