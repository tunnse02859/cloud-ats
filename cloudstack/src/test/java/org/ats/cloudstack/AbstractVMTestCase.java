/**
 * 
 */
package org.ats.cloudstack;

import org.apache.cloudstack.api.ApiConstants.VMDetails;
import org.apache.cloudstack.jobs.JobInfo.Status;
import org.ats.cloudstack.model.Job;
import org.ats.cloudstack.model.VirtualMachine;
import org.junit.After;
import org.junit.Before;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 26, 2014
 */
public class AbstractVMTestCase {
  /** .*/
  protected VirtualMachine vm;
  
  @Before
  public void setUp() throws Exception {
    this.vm = this.createVM("jenkins-slave-non-ui"); 
  }
  
  protected VirtualMachine createVM(String vmTemplate) throws Exception {
    String[] response = VirtualMachineAPI.quickDeployVirtualMachine(
        "slave-" + System.currentTimeMillis(), vmTemplate, "Medium Instance", null);
    String vmId = response[0];
    String jobId = response[1];
    Job job = AsyncJobAPI.queryAsyncJobResult(jobId);
    while (!job.getStatus().done()) {
      job = AsyncJobAPI.queryAsyncJobResult(jobId);
    }
    if (job.getStatus() == Status.SUCCEEDED) {
      System.out.println("Created VM: " + vmId);
      return VirtualMachineAPI.findVMById(vmId, VMDetails.nics);
    }
    
    return null;
  }
  
  @After
  public void tearDown() throws Exception {
    Thread.sleep(15 * 1000); //unstable
    
    String jobId = VirtualMachineAPI.destroyVM(vm.id, true);
    Job job = AsyncJobAPI.queryAsyncJobResult(jobId);
    while (!job.getStatus().done()) {
      job = AsyncJobAPI.queryAsyncJobResult(jobId);
    }
    if (job.getStatus() == Status.SUCCEEDED) {
      System.out.println("Destroyed VM: " + vm.id);
      VolumeAPI.clearNotAttachedVolumes();
    }
  }
}
