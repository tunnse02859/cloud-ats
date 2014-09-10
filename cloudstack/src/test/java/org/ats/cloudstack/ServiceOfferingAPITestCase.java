package org.ats.cloudstack;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.ats.cloudstack.model.ServiceOffering;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 * 
 */

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class ServiceOfferingAPITestCase {
  
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
  public void listServiceOfferings() throws Exception {
    List<ServiceOffering> list = ServiceOfferingAPI.listServiceOfferings(client, null, null);
    Assert.assertEquals(3, list.size());
    list = ServiceOfferingAPI.listServiceOfferings(client, null, "Small Instance");
    Assert.assertEquals(1, list.size());
    
    ServiceOffering so = list.get(0);
    Assert.assertEquals(1, so.cpuNumber);
    Assert.assertEquals(500, so.cpuSpeed);
    Assert.assertEquals(512, so.memory);
  }
}
