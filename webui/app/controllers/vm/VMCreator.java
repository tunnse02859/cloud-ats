/**
 * 
 */
package controllers.vm;

import helpervm.OfferingHelper;
import helpervm.VMHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;

import models.vm.OfferingModel;
import models.vm.VMModel;
import models.vm.VMModel.VMStatus;

import org.ats.cloudstack.AsyncJobAPI;
import org.ats.cloudstack.CloudStackClient;
import org.ats.cloudstack.VirtualMachineAPI;
import org.ats.cloudstack.model.Job;
import org.ats.cloudstack.model.VirtualMachine;
import org.ats.common.ssh.SSHClient;
import org.ats.component.usersmgt.group.Group;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsSlave;
import org.ats.knife.Knife;

import play.Logger;
import azure.AzureClient;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.management.compute.models.RoleInstance;
import com.microsoft.windowsazure.management.compute.models.VirtualMachineRoleSize;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 19, 2014
 */
public class VMCreator {
  
  public static VMModel createCompanySystemVM(Group company) throws Exception {
    
    AzureClient azureClient = VMHelper.getAzureClient();
    
    String normalName = new StringBuilder().append(company.getString("name")).append("-jenkins").toString();
    String name = normalizeVMName(new StringBuilder(normalName).append("-").append(company.getId()).toString());
    
    Future<OperationStatusResponse> response = azureClient.createSystemVM(name);
    OperationStatusResponse status = response.get();
    Logger.info("Create system vm " + name + " has been " + status.getStatus());
    
    
    RoleInstance vm = azureClient.getVirutalMachineByName(name);
    
    VMModel vmModel = new VMModel(vm.getRoleName(), name, company.getId(), "cats-sys-image", "cats-sys-image", 
        vm.getIPAddress().getHostAddress(), VMHelper.getSystemProperty("default-user"), VMHelper.getSystemProperty("default-password"));
    vmModel.put("system", true);
    vmModel.put("jenkins", true);
    vmModel.put("offering_id", VirtualMachineRoleSize.MEDIUM);
    vmModel.put("normal_name", normalName);
    VMHelper.createVM(vmModel);

    List<OfferingModel> list = OfferingHelper.getEnableOfferings();
    Collections.sort(list, new Comparator<OfferingModel>() {
      @Override
      public int compare(OfferingModel o1, OfferingModel o2) {
        return o2.getMemory() - o1.getMemory();
      }
    });

    OfferingModel defaultOffering = list.get(0);
    OfferingHelper.addDefaultOfferingForGroup(company.getId(), defaultOffering.getId());
    return vmModel;
  }
  
  public static VMModel createNormalGuiVM(Group company) throws Exception {
    return createNormalVM(company, "Gui", "jenkins-slave", "jenkins-slave-gui");
  }
  
  public static VMModel createNormalNonGuiVM(Group company) throws Exception {
    return createNormalVM(company, "Non-Gui", "jenkins-slave-non-ui", "jenkins-slave", "jmeter");
  }
  
  public static Map<String, ConcurrentLinkedQueue<String>> QueueHolder = new HashMap<String, ConcurrentLinkedQueue<String>>();

  public static VMModel createNormalVM(Group company, String subfix, String template, final String ...recipes) throws IOException, JSchException {
    CloudStackClient client = VMHelper.getCloudStackClient();
    OfferingModel offering = OfferingHelper.getDefaultOfferingOfGroup(company.getId()).getOffering();
    final VMModel jenkins = VMHelper.getVMsByGroupID(company.getId(), new BasicDBObject("system", true).append("jenkins", true)).get(0);
    
    //create instance
    String normalName = getAvailableName(company, subfix, 0);
    final String name = normalizeVMName(new StringBuilder(normalName).append("-").append(company.getId()).toString());
    QueueHolder.put(name, new ConcurrentLinkedQueue<String>());
    
    String[] response = VirtualMachineAPI.quickDeployVirtualMachine(client, name, template, offering.getName(), null);
    String vmId = response[0];
    String jobId = response[1];
    Job job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
    while (!job.getStatus().done()) {
      job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
    }
    
    if (job.getStatus() == org.apache.cloudstack.jobs.JobInfo.Status.SUCCEEDED) {
      VirtualMachine guiVM = VirtualMachineAPI.findVMById(client, vmId, null);
      final VMModel vmModel = new VMModel(guiVM.id, name, company.getId(), guiVM.templateName, guiVM.templateId, guiVM.nic[0].ipAddress, "ubuntu", "ubuntu");
      vmModel.put("gui", "Non-Gui".equals(subfix) ? false : true);
      vmModel.put("system", false);
      vmModel.put("offering_id", guiVM.serviceOfferingId);
      vmModel.setStatus(VMStatus.Initializing);
      vmModel.put("normal_name", normalName);
      VMHelper.createVM(vmModel);

      //Run recipes
      Thread thread = new Thread() {
        @Override
        public void run() {
          ConcurrentLinkedQueue<String> queue = QueueHolder.get(name);
          try {
            queue.add("Checking SSHD on " + vmModel.getPublicIP());
            if (SSHClient.checkEstablished(vmModel.getPublicIP(), 22, 300)) {
              queue.add("Connection is established");
              
              Session session = SSHClient.getSession(vmModel.getPublicIP(), 22, vmModel.getUsername(), vmModel.getPassword());
              
              //sudo
              ChannelExec channel = (ChannelExec) session.openChannel("exec");
              String command = "echo '" + jenkins.getPublicIP() + "' > ~/jenkins.master";
              channel.setCommand(command);
              channel.connect();
              queue.add("Execute command: " + command);
              channel.disconnect();
              
              //TODO:Workaround for unclean vm template
              channel = (ChannelExec) session.openChannel("exec");
              command = "sudo -S -p '' rm /etc/chef/client.rb";
              channel.setCommand(command);
              OutputStream out = channel.getOutputStream();
              channel.connect();
              
              out.write((vmModel.getPassword() + "\n").getBytes());
              out.flush();
              channel.disconnect();
              
              channel = (ChannelExec) session.openChannel("exec");
              command = "sudo -S -p '' rm /etc/chef/client.pem";
              channel.setCommand(command);
              out = channel.getOutputStream();
              channel.connect();
              
              out.write((vmModel.getPassword() + "\n").getBytes());
              out.flush();
              channel.disconnect();
              //End
              
              Knife knife = VMHelper.getKnife();
              knife.bootstrap(vmModel.getPublicIP(), vmModel.getName(), queue, recipes);
              
            } else {
              queue.add("Cloud not establish connection in 120s");
            }
          } catch (Exception e) {
            Logger.debug("Has an error when create vm", e);
            queue.add("setup.vm.error");
          } finally {
            queue.add("log.exit");
          }
        }
      };
      thread.start();
      
      return vmModel;
    }
    
    return null;
  }
  
  public static String getAvailableName(Group company, String subfix, int index) throws IOException, JSchException {
    CloudStackClient client = VMHelper.getCloudStackClient();
    
    String normalName = new StringBuilder(company.getString("name")).append("-").append(subfix).append("-").append(index).toString();

    String name = normalizeVMName(new StringBuilder(normalName).append("-").append(company.getId()).toString());
    
    //remove legacy vm
    List<VMModel> vms = VMHelper.getVMs(new BasicDBObject("group_id", company.getId()).append("system", false));
    VMModel jenkins = VMHelper.getVMsByGroupID(company.getId(), new BasicDBObject("jenkins", true)).get(0);
    
    for (VMModel vm : vms) {
      if (VirtualMachineAPI.findVMById(client, vm.getId(), null) == null) {
        VMHelper.removeVM(vm);
        VMHelper.getKnife().deleteNode(vm.getName());
        new JenkinsSlave(new JenkinsMaster(jenkins.getPublicIP(), "http", 8080), vm.getPublicIP()).release();
      }
    }
    
    List<VirtualMachine> list = VirtualMachineAPI.listVirtualMachines(client, null, name, null, null, null);
    if (list.size() == 0) return normalName;
    return getAvailableName(company, subfix, index + 1);
  }
  
  /**
   * Instance name can not be longer than 63 characters. 
   * Only ASCII letters a~z, A~Z, digits 0~9, hyphen are allowed. Must start with a letter and end with a letter or a digit.
   */
  public static String normalizeVMName(String name) {
    StringBuilder sb = new StringBuilder();
    char[] chars = name.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      char ch = chars[i];
      if (ch >= 'a' && ch <= 'z') sb.append(ch);
      else if (ch >= 'A' && ch <= 'Z') sb.append(ch);
      else if (ch >= '0' && ch <= '9') sb.append(ch);
      else if (ch == '-' && i != 0 && i != 62 && i != chars.length -1) sb.append(ch);
      else {
        if (i == 0) sb.append('a');
        else if (i == 62) sb.append('a');
        else if (i == chars.length -1) sb.append('z');
        else sb.append('-');
      }
    }
    name = sb.length() > 63 ? sb.substring(0, 63) : sb.toString();
    return name;
  }
}
