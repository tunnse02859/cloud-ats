/**
 * 
 */
package azure;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jan 13, 2015
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.Future;

import org.ats.common.ssh.SSHClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.management.compute.models.DeploymentGetResponse;
import com.microsoft.windowsazure.management.compute.models.DeploymentStatus;
import com.microsoft.windowsazure.management.compute.models.RoleInstance;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineRoleSize;
import com.microsoft.windowsazure.management.models.RoleSizeListResponse.RoleSize;

public class AzureTest {
  
  private static AzureClient client = null;
  
  @BeforeClass
  public static void init() throws Exception {
    Properties properties = new Properties();
    properties.load(new FileInputStream("/home/haint/cloud-ats.properties"));
    client = new AzureClient(
        properties.getProperty("subcriptionId"),
        properties.getProperty("keystoreLocation"), properties.getProperty("keystorePassword"));
    
    DeploymentGetResponse response = client.getComputeManagementClient().getDeploymentsOperations().getByName("cloud-ats", "cats-portal");
    System.out.println(response.getStatus());
    if (response.getStatus() != DeploymentStatus.Running) {
      client.getComputeManagementClient().getVirtualMachinesOperations().start("cloud-ats", "cats-portal", "cats-portal");
//      client.getComputeManagementClient().getDeploymentsOperations().
    }
  }
  
  @Before
  public void setup() throws IOException {
    
  }

  //@Test
  public void testGetOfferingDetail() throws Exception {
    RoleSize extraSmall = client.getOfferingByName(VirtualMachineRoleSize.EXTRASMALL);
    Assert.assertEquals(1, extraSmall.getCores());
    Assert.assertEquals(768, extraSmall.getMemoryInMb());
    System.out.println(extraSmall.getLabel());
    
    RoleSize small = client.getOfferingByName(VirtualMachineRoleSize.SMALL);
    Assert.assertEquals(1, small.getCores());
    Assert.assertEquals(1792, small.getMemoryInMb());
    System.out.println(small.getLabel());
    
    RoleSize medium = client.getOfferingByName(VirtualMachineRoleSize.MEDIUM);
    Assert.assertEquals(2, medium.getCores());
    Assert.assertEquals(3584, medium.getMemoryInMb());
    System.out.println(medium.getLabel());
  }
  
  @Test
  public void testListVirtualMachine() throws Exception {
    RoleInstance vm = client.getVirutalMachineByName("cats-ui");
    System.out.println(vm.getIPAddress().getHostAddress());
    System.out.println(vm.getPublicIPs().size());
    System.out.println(vm.getNetworkInterfaces().size());
    System.out.println(vm.getHostName());
    
    //System.out.println(vm.getPublicIPs().get(0).getAddress());
  }
  
  //@Test
  public void testCreateSystemVM() throws Exception {
    Future<OperationStatusResponse> result = client.createSystemVM("cats-group-sys");
    tracking(result);
    
    RoleInstance vm = client.getVirutalMachineByName("cats-group-sys");
    Assert.assertNotNull(vm);
    
    result = client.deleteVirtualMachineByName("cats-group-sys");
    tracking(result);
    
    vm = client.getVirutalMachineByName("cats-group-sys");
    Assert.assertNull(vm);
  }
  
  //@Test
  public void testStartStopVMs() throws Exception {
    RoleInstance vm1 = client.getVirutalMachineByName("cats-ui");
    Assert.assertEquals("cats-ui", vm1.getRoleName());
    Assert.assertEquals("StoppedVM", vm1.getInstanceStatus());
    
    RoleInstance vm2 = client.getVirutalMachineByName("cats-non-ui");
    Assert.assertEquals("cats-non-ui", vm2.getRoleName());
    Assert.assertEquals("StoppedVM", vm2.getInstanceStatus());
    
    RoleInstance vm3 = client.getVirutalMachineByName("cats-sys");
    Assert.assertEquals("cats-sys", vm3.getRoleName());
    Assert.assertEquals("StoppedVM", vm3.getInstanceStatus());
    
    Future<OperationStatusResponse> response = client.startVirtualMachinesByNames("cats-ui", "cats-non-ui", "cats-sys");
    tracking(response);

    System.out.println(response.get().getId());
    System.out.println(response.get().getRequestId());
    System.out.println(response.get().getStatusCode());
    System.out.println(response.get().getError());
    System.out.println(response.get().getHttpStatusCode());
    System.out.println(response.get().getStatus());

    vm1 = client.getVirutalMachineByName("cats-ui");
    Assert.assertEquals("cats-ui", vm1.getRoleName());
    System.out.println(vm1.getInstanceStatus());

    vm2 = client.getVirutalMachineByName("cats-non-ui");
    Assert.assertEquals("cats-non-ui", vm2.getRoleName());
    System.out.println(vm2.getInstanceStatus());

    vm3 = client.getVirutalMachineByName("cats-sys");
    Assert.assertEquals("cats-sys", vm3.getRoleName());
    System.out.println(vm3.getInstanceStatus());

    ArrayList<RoleInstance> vms = client.waitVirtualMachinesForStatus("ReadyRole", 5*60*1000, "cats-ui", "cats-non-ui", "cats-sys");
    Assert.assertEquals(3, vms.size());
    for (RoleInstance vm : vms) {
      Assert.assertEquals("ReadyRole", vm.getInstanceStatus());
    }
    
    response = client.stopVirtualMachinesByNames("cats-ui", "cats-non-ui", "cats-sys");
    tracking(response);
    
    System.out.println(response.get().getId());
    System.out.println(response.get().getRequestId());
    System.out.println(response.get().getStatusCode());
    System.out.println(response.get().getError());
    System.out.println(response.get().getHttpStatusCode());
    System.out.println(response.get().getStatus());
    
    vms = client.waitVirtualMachinesForStatus("StoppedVM", 5*60*1000, "cats-ui", "cats-non-ui", "cats-sys");
    Assert.assertEquals(3, vms.size());
    for (RoleInstance vm : vms) {
      Assert.assertEquals("StoppedVM", vm.getInstanceStatus());
    }
  }
  
  private Future<?> tracking(Future<?> future) throws InterruptedException {
    while (!future.isDone()) {
      System.out.print('.');
      Thread.sleep(1000);
    }
    return future;
  }
  @Test
  public void testExecuteCommand() throws JSchException{
    Session session = SSHClient.getSession("10.32.0.5", 22, "azureuser", "#CloudATS");    
    //sudo
    ChannelExec channel = (ChannelExec) session.openChannel("exec");
    String command = "nohup jmeter-start > log.log 2>&1 &";
    channel.setCommand(command);
    channel.connect();
    System.out.println("connect success");
    //channel.run();                  
    channel.disconnect();
  }
}
