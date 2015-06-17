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
 *         Jun 17, 2015
 */
public class JMeterSamplerTestCase {

  @Test
  public void testCRUD() throws Exception {
    JMeterFactory factory = new JMeterFactory();

    List<JMeterArgument> arguments = new ArrayList<JMeterArgument>();
    JMeterArgument param1 = factory.createArgument("username", "admin");
    JMeterArgument param2 = factory
        .createArgument("password", "admin.password");
    arguments.add(param1);
    arguments.add(param2);

    JMeterSampler jMeterSampler = new JMeterSampler(Method.POST, "Register",
        "http://localhost:8080/signup", null, 0, arguments);
    jMeterSampler.setName("SignUp");
    Assert.assertEquals(jMeterSampler.getName(), "SignUp");
    Assert.assertEquals(jMeterSampler.getArguments().size(), 2);
    
    JMeterArgument param3 = factory.createArgument("re-password", "admin.password");
    jMeterSampler.addArgument(param3);
    Assert.assertEquals(jMeterSampler.getArguments().size(), 3);
  }
}
