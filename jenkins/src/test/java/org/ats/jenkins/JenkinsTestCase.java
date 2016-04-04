/**
 * 
 */
package org.ats.jenkins;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 14, 2015
 */
public class JenkinsTestCase {
  
  @Test
  public void testUbuntu() throws IOException {
    JenkinsMaster master = new JenkinsMaster("localhost", "http", "/jenkins", 8080);
    System.setProperty("jenkins.slave.credential", "965a0c50-868c-48b1-8f3e-b0179bf40666");
    JenkinsSlave slave = new JenkinsSlave(master, "test.local", false);
    Assert.assertEquals(slave.getCredential(), "965a0c50-868c-48b1-8f3e-b0179bf40666");
    Assert.assertTrue(slave.getSlaveTemplate().indexOf("965a0c50-868c-48b1-8f3e-b0179bf40666") != -1);
    Assert.assertTrue(slave.getSlaveJsonTemplate().indexOf("965a0c50-868c-48b1-8f3e-b0179bf40666") != -1 && slave.getSlaveJsonTemplate().indexOf("test.local") != -1);
  }

  @Test
  public void testWindows() throws Exception {
    JenkinsMaster master = new JenkinsMaster("172.27.4.243", "http", "/jenkins", 8080);
    System.setProperty("jenkins.slave.credential", "965a0c50-868c-48b1-8f3e-b0179bf40666");
    JenkinsSlave slave = new JenkinsSlave(master, "192.168.1.115", true);
    slave.join();
  }
}
