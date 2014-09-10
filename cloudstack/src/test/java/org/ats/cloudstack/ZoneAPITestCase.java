/**
 * 
 */
package org.ats.cloudstack;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.ats.cloudstack.model.Zone;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class ZoneAPITestCase {
  
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
  public void listAvailableZone() throws Exception {
    List<Zone> zones = ZoneAPI.listAvailableZones(client);
    Assert.assertEquals(1, zones.size());
    Zone zone = zones.get(0);
    Assert.assertEquals("myzone", zone.name);
  }
}
