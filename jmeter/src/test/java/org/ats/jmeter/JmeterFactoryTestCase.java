/**
 * 
 */
package org.ats.jmeter;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 9, 2014
 */
public class JmeterFactoryTestCase {

  @Test
  public void testCreatePom() {
    try {
      JmeterFactory factory = new JmeterFactory();
      String pom = factory.createPom("org.ats.cloud", "cloud.test");
      Assert.assertTrue(pom.indexOf("org.ats.cloud") != -1);
      Assert.assertTrue(pom.indexOf("cloud.test") != -1);
    } catch (Exception e) {
      Assert.fail();
    }
  }
  
  @Test
  public void testCreateArgument() throws Exception {
    JmeterFactory factory = new JmeterFactory();
    String argument = factory.createArgument("username", "admin");
    Assert.assertTrue(argument.indexOf("<stringProp name=\"Argument.name\">username</stringProp>") != -1);
    Assert.assertTrue(argument.indexOf("<stringProp name=\"Argument.value\">admin</stringProp>") != -1);
  }
  
  @Test
  public void testCreateArguments() throws Exception {
    JmeterFactory factory = new JmeterFactory();
    String param1 = factory.createArgument("username", "admin");
    String param2 = factory.createArgument("password", "admin.password");
    String arguments = factory.createArguments(param1, param2);
    
    Assert.assertTrue(arguments.indexOf("<stringProp name=\"Argument.name\">username</stringProp>") != -1);
    Assert.assertTrue(arguments.indexOf("<stringProp name=\"Argument.value\">admin</stringProp>") != -1);
    
    Assert.assertTrue(arguments.indexOf("<stringProp name=\"Argument.name\">password</stringProp>") != -1);
    Assert.assertTrue(arguments.indexOf("<stringProp name=\"Argument.value\">admin.password</stringProp>") != -1);
  }
  
  @Test
  public void testCreateHttpGet() throws Exception {
    JmeterFactory factory = new JmeterFactory();
    String param1 = factory.createArgument("username", "admin");
    String param2 = factory.createArgument("password", "admin.password");
    
    String sampleGet1 = factory.createHttpGet("login", "http://172.27.4.48:8080/signin", null, 0, param1, param2);
    System.out.println(sampleGet1);
    
    String sampleGet2 = factory.createHttpGet("login", "http://172.27.4.48:8080/signin?username=admin&password=admin.password", null, 0);
//    System.out.println(sampleGet2);
    Assert.assertEquals(sampleGet1, sampleGet2);
  }
  
  @Test
  public void testCreateHttpPost() throws Exception {
    JmeterFactory factory = new JmeterFactory();
    String param1 = factory.createArgument("username", "admin");
    String param2 = factory.createArgument("password", "admin.password");
    
    String samplePost = factory.createHttpPost("login", "http://172.27.4.48:8080/signin", null, 0, param1, param2);
    System.out.println(samplePost);
  }
  
  @Test
  public void testCreateJmeterScript() throws Exception {
    JmeterFactory factory = new JmeterFactory();
    String signinRequest = factory.createHttpGet("Signin Page", "http://172.27.4.48:9000/signin", null, 0);
    String loginPost = factory.createHttpPost("Login", "http://172.27.4.48:9000/signin", null, 0,
        factory.createArgument("email", "root@system.com"),
        factory.createArgument("password", "admin"));
    
    String oRequest = factory.createHttpGet("Organization Page", 
        "http://172.27.4.48:9000/portal/o?nav=group&group=40d4edcd-ff1b-483f-9b69-50aff29f49f6", null, 0);
    
    String signoutRequest = factory.createHttpGet("Signout", "http://172.27.4.48:9000/signout", null, 0);
    
    String jmeter = factory.createJmeterScript(
        1, 100, 5, false, 0,
        signinRequest, loginPost, oRequest, signoutRequest);
    System.out.println(jmeter);
  }
}
