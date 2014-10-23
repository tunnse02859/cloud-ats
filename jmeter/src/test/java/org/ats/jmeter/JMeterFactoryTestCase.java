/**
 * 
 */
package org.ats.jmeter;

import junit.framework.Assert;

import org.ats.jmeter.models.JMeterArgument;
import org.ats.jmeter.models.JMeterSampler;
import org.ats.jmeter.models.JMeterScript;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 9, 2014
 */
public class JMeterFactoryTestCase {

  @Test
  public void testCreatePom() {
    try {
      JMeterFactory factory = new JMeterFactory();
      String pom = factory.createPom("org.ats.cloud", "cloud.test");
      Assert.assertTrue(pom.indexOf("org.ats.cloud") != -1);
      Assert.assertTrue(pom.indexOf("cloud.test") != -1);
    } catch (Exception e) {
      Assert.fail();
    }
  }
  
  @Test
  public void testCreateArgument() throws Exception {
    JMeterFactory factory = new JMeterFactory();
    String argument = factory.createArgument("username", "admin").toString();
    Assert.assertTrue(argument.indexOf("<stringProp name=\"Argument.name\">username</stringProp>") != -1);
    Assert.assertTrue(argument.indexOf("<stringProp name=\"Argument.value\">admin</stringProp>") != -1);
  }
  
  @Test
  public void testCreateArguments() throws Exception {
    JMeterFactory factory = new JMeterFactory();
    JMeterArgument param1 = factory.createArgument("username", "admin");
    JMeterArgument param2 = factory.createArgument("password", "admin.password");
    String arguments = factory.createArguments(param1, param2);
    
    Assert.assertTrue(arguments.indexOf("<stringProp name=\"Argument.name\">username</stringProp>") != -1);
    Assert.assertTrue(arguments.indexOf("<stringProp name=\"Argument.value\">admin</stringProp>") != -1);
    
    Assert.assertTrue(arguments.indexOf("<stringProp name=\"Argument.name\">password</stringProp>") != -1);
    Assert.assertTrue(arguments.indexOf("<stringProp name=\"Argument.value\">admin.password</stringProp>") != -1);
  }
  
  @Test
  public void testCreateHttpGet() throws Exception {
    JMeterFactory factory = new JMeterFactory();
    JMeterArgument param1 = factory.createArgument("username", "admin");
    JMeterArgument param2 = factory.createArgument("password", "admin.password");
    
    JMeterSampler sampleGet1 = factory.createHttpGet("login", "http://172.27.4.48:8080/signin", null, 0, param1, param2);
    
    JMeterSampler sampleGet2 = factory.createHttpGet("login", "http://172.27.4.48:8080/signin?username=admin&password=admin.password", null, 0);
    Assert.assertNotSame(sampleGet1, sampleGet2);
    Assert.assertEquals(sampleGet1.toString(), sampleGet2.toString());
  }
  
  @Test
  public void testCreateHttpPost() throws Exception {
    JMeterFactory factory = new JMeterFactory();
    JMeterArgument param1 = factory.createArgument("username", "admin");
    JMeterArgument param2 = factory.createArgument("password", "admin.password");
    
    JMeterSampler samplePost = factory.createHttpPost("login", "http://172.27.4.48:8080/signin", null, 0, param1, param2);
    System.out.println(samplePost);
  }
  
  @Test
  public void testCreateJmeterScript() throws Exception {
    JMeterFactory factory = new JMeterFactory();
    JMeterSampler signinRequest = factory.createHttpGet("Signin Page", "http://172.27.4.48:9000/signin", null, 0);
    JMeterSampler loginPost = factory.createHttpPost("Login", "http://172.27.4.48:9000/signin", null, 0,
        factory.createArgument("email", "root@system.com"),
        factory.createArgument("password", "admin"));
    
    JMeterSampler oRequest = factory.createHttpGet("Organization Page", 
        "http://172.27.4.48:9000/portal/o?nav=group&group=40d4edcd-ff1b-483f-9b69-50aff29f49f6", null, 0);
    
    JMeterSampler signoutRequest = factory.createHttpGet("Signout", "http://172.27.4.48:9000/signout", null, 0);
    
    JMeterScript jmeter = factory.createJmeterScript(
        "Test Name",
        1, 100, 5, false, 0,
        signinRequest, loginPost, oRequest, signoutRequest);
    System.out.println(jmeter);
  }
}
