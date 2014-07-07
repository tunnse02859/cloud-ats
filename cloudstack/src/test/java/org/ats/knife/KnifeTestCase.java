/**
 * 
 */
package org.ats.knife;

import junit.framework.Assert;

import org.ats.cloudstack.AbstractVMTestCase;
import org.ats.common.ssh.SSHClient;
import org.junit.Test;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 26, 2014
 */
public class KnifeTestCase extends AbstractVMTestCase {
  
  @Override
  public void tearDown() throws Exception {
    Knife.getInstance().deleteNode(vm.name);
    super.tearDown();
  }
  
  @Test
  public void testBootstrapAndDelete() throws Exception {
    
    String ipAddress = vm.nic[0].ipAddress;
    
    Knife knife = Knife.getInstance();
    
    if (SSHClient.checkEstablished(ipAddress, 22, 120)) {
      knife.bootstrap(ipAddress, vm.name, "jenkins-slave");
    } else {
      Assert.fail("Can not establish ssh connection for " + ipAddress);
    }
  }
}
