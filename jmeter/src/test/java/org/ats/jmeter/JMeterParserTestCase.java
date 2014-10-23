/**
 * 
 */
package org.ats.jmeter;

import java.io.FileOutputStream;
import java.io.IOException;

import org.ats.jmeter.models.JMeterArgument;
import org.ats.jmeter.models.JMeterSampler;
import org.ats.jmeter.models.JMeterScript;
import org.ats.jmeter.models.JMeterSampler.Method;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class JMeterParserTestCase {
  
  /** .*/
  String source;
  
  @Before
  public void setUp() throws IOException {
    JMeterFactory factory = new JMeterFactory();
    JMeterSampler signinRequest = factory.createHttpGet("Signin Page", "http://172.27.4.48:9000/signin", "this is assertion text", 1000);
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
    
    this.source = jmeter.toString();
    FileOutputStream fos = new FileOutputStream("target/script.jmx");
    fos.write(this.source.getBytes("UTF-8"));
    fos.close();
  }

  @Test
  public void test() throws Exception {
    JMeterFactory factory = new JMeterFactory();
    JMeterParser parser = factory.createJMeterParser(source);
    JMeterScript script = parser.parse();
    
    Assert.assertEquals("Test Name", script.getName());
    Assert.assertEquals(1, script.getLoops());
    Assert.assertEquals(100, script.getNumberThreads());
    Assert.assertEquals(5, script.getRamUp());
    Assert.assertFalse(script.isScheduler());
    Assert.assertEquals(0, script.getDuration());
    
    Assert.assertEquals(4, script.getSamplers().length);
    
    JMeterSampler signinRequest = script.getSamplers()[0];
    Assert.assertEquals("Signin Page", signinRequest.getName());
    Assert.assertEquals("http://172.27.4.48:9000/signin", signinRequest.getUrl());
    Assert.assertEquals(Method.GET, signinRequest.getMethod());
    Assert.assertEquals("this is assertion text", signinRequest.getAssertionText());
    Assert.assertEquals(1000, signinRequest.getContantTime());
    
    JMeterSampler loginPost = script.getSamplers()[1];
    Assert.assertEquals("Login", loginPost.getName());
    Assert.assertEquals("http://172.27.4.48:9000/signin", loginPost.getUrl());
    Assert.assertEquals(Method.POST, loginPost.getMethod());
    Assert.assertNull(loginPost.getAssertionText());
    Assert.assertEquals(0, loginPost.getContantTime());
    
    Assert.assertEquals(2, loginPost.getArguments().length);
    
    JMeterArgument email = loginPost.getArguments()[0];
    Assert.assertEquals("email", email.getParamName());
    Assert.assertEquals("root@system.com", email.getParamValue());
    
    JMeterArgument password = loginPost.getArguments()[1];
    Assert.assertEquals("password", password.getParamName());
    Assert.assertEquals("admin", password.getParamValue());
    
    JMeterSampler oRequest = script.getSamplers()[2];
    Assert.assertEquals("Organization Page", oRequest.getName());
    Assert.assertEquals("http://172.27.4.48:9000/portal/o", oRequest.getUrl());
    Assert.assertEquals(Method.GET, oRequest.getMethod());
    Assert.assertNull(oRequest.getAssertionText());
    Assert.assertEquals(0, oRequest.getContantTime());
    
    Assert.assertEquals(2, oRequest.getArguments().length);
    
    JMeterArgument nav = oRequest.getArguments()[0];
    Assert.assertEquals("nav", nav.getParamName());
    Assert.assertEquals("group", nav.getParamValue());
    
    JMeterArgument group = oRequest.getArguments()[1];
    Assert.assertEquals("group", group.getParamName());
    Assert.assertEquals("40d4edcd-ff1b-483f-9b69-50aff29f49f6", group.getParamValue());
    
    JMeterSampler signoutRequest = script.getSamplers()[3];
    Assert.assertEquals("Signout", signoutRequest.getName());
    Assert.assertEquals("http://172.27.4.48:9000/signout", signoutRequest.getUrl());
    Assert.assertEquals(Method.GET, signoutRequest.getMethod());
    Assert.assertNull( signoutRequest.getAssertionText());
    Assert.assertEquals(0, signoutRequest.getContantTime());
  }
}
