/**
 * 
 */
package org.ats.cloudstack;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.ats.cloudstack.model.Template;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cloud.template.VirtualMachineTemplate.TemplateFilter;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class TemplateAPITestCase {

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
  public void listTemplates() throws Exception {
    List<Template> templates = TemplateAPI.listTemplates(client, TemplateFilter.all, null, "jenkins-slave",  null);
    Assert.assertEquals(1, templates.size());
  }
}
