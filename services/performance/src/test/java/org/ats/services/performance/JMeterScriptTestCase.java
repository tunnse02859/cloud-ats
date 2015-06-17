/**
 * 
 */
package org.ats.services.performance;

import java.util.ArrayList;
import java.util.List;

import org.ats.services.performance.JMeterSampler.Method;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author NamBV2
 *
 * Jun 17, 2015
 */
public class JMeterScriptTestCase {

  @Test
  public void testCRUD() throws Exception {
    
    JMeterFactory factory = new JMeterFactory();
    JMeterSampler signinRequest = factory.createHttpGet("Signin Page", "http://localhost:9000/signin", "this is assertion text", 1000);
    JMeterSampler loginPost = factory.createHttpPost("Login", "http://localhost:9000/signin", null, 0,
        factory.createArgument("email", "root@system.com"),
        factory.createArgument("password", "admin"));
    JMeterSampler oRequest = factory.createHttpGet("Organization Page", 
        "http://localhost:9000/portal/o?nav=group&group=40d4edcd-ff1b-483f-9b69-50aff29f49f6", null, 0);
    JMeterSampler signoutRequest = factory.createHttpGet("Signout", "http://localhost:9000/signout", null, 0);
    JMeterScript jmeter = factory.createJmeterScript(
        "Test Name",
        1, 100, 5, false, 0,
        signinRequest, loginPost, oRequest, signoutRequest);
    
    jmeter.setName("Test Script");
    Assert.assertEquals(jmeter.getName(), "Test Script");
    
    Assert.assertEquals(jmeter.getSamplers().size(), 4);
    
    //Create new a JmeterSampler
    List<JMeterArgument> arguments = new ArrayList<JMeterArgument>();
    JMeterArgument param = factory
        .createArgument("password", "admin.password");
    arguments.add(param);
    JMeterSampler newSampler = new JMeterSampler(Method.POST, "Register",
        "http://localhost:8080/signup", null, 0, arguments);
    jmeter.addSampler(newSampler);
    Assert.assertEquals(jmeter.getSamplers().size(), 5);
  }
}
