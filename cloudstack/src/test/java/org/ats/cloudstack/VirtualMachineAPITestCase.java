/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiConstants.VMDetails;
import org.apache.cloudstack.jobs.JobInfo.Status;
import org.ats.cloudstack.model.Job;
import org.ats.cloudstack.model.VirtualMachine;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ats.common.ssh.SSHClient;

import com.cloud.vm.VirtualMachine.State;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class VirtualMachineAPITestCase {
  
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
  
  @AfterClass 
  public static void tearDown() throws IOException {
    VolumeAPI.clearNotAttachedVolumes();
  }
  
  @Test
  public void testListVirutalMachines() throws IOException {
    List<VirtualMachine> vms = VirtualMachineAPI.listVirtualMachines(client, null, "chef-workstation", State.Running, null, ApiConstants.VMDetails.nics);
    Assert.assertEquals(1, vms.size());
    VirtualMachine vm = vms.get(0);
    Assert.assertEquals("chef-workstation", vm.name);
    Assert.assertEquals("chef-workstation", vm.displayName);
    Assert.assertEquals(1, vm.nic.length);
    Assert.assertEquals("172.27.4.86", vm.nic[0].ipAddress);
    
    vms = VirtualMachineAPI.listVirtualMachines(client, null, "not-existed", State.Unknown, "not-existed", null);
    Assert.assertEquals(0, vms.size());
  }
  
  @Test
  public void quickDeployAndDestroy() throws Exception {
    String[] response = VirtualMachineAPI.quickDeployVirtualMachine(client, "test-" + System.currentTimeMillis(), "jenkins-slave-non-ui", "Small Instance", "Small");
    String vmId = response[0];
    String jobId = response[1];
    Job job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
    while (!job.getStatus().done()) {
      job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
    }
    
    if (job.getStatus() == Status.SUCCEEDED) {
      
      VirtualMachine vm = VirtualMachineAPI.findVMById(client, vmId, VMDetails.nics);
      String ipAddress = vm.nic[0].ipAddress;
      System.out.println("Created VM: " + ipAddress);
      
      if (SSHClient.checkEstablished(ipAddress, 22, 120)) {
        jobId = VirtualMachineAPI.destroyVM(client, vmId, true);
        job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
        while (!job.getStatus().done()) {
          job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
        }
        if (job.getStatus() == Status.SUCCEEDED) {
          System.out.println("Destroyed VM: " + vmId);
          return;
        } else {
          Assert.fail("Destroy VM Unsuccessful");
        }
      } else {
        Assert.fail("Can not establish ssh connection for " + ipAddress);
      }
    } else {
      Assert.fail("Create VM Unsuccessful");
    }
  }
}
