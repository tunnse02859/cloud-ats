/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.ats.cloudstack.model.Volume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 26, 2014
 */
public class VolumeAPITestCase {
  
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
  public void listVolumes() throws Exception {
    List<Volume> list = VolumeAPI.listVolumes(client, null, null, "DATADISK", null, null);
    System.out.println(list.size());
    List<Volume> deattached = VolumeAPI.listVolumesNotAttached(null, null, "DATADISK", null);
    System.out.println(deattached.size());
  }
  
  @Test
  public void clearNotAttachedVolumes() throws IOException {
    Assert.assertTrue(VolumeAPI.clearNotAttachedVolumes());
  }
}
