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

import org.ats.common.ssh.SSHClient;
import org.ats.component.usersmgt.group.Group;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsSlave;
import org.ats.knife.Knife;

import play.Logger;
import azure.AzureClient;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import com.microsoft.windowsazure.core.OperationStatusResponse;
import com.microsoft.windowsazure.management.compute.models.RoleInstance;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 19, 2014
 */
public class VMCreator {
  
  public static void createCompanySystemVM(Group company) throws Exception {
    
    AzureClient azureClient = VMHelper.getAzureClient();
    
    String normalName = new StringBuilder().append(company.getString("name")).append("-system").toString();
    String name = normalizeVMName(new StringBuilder(normalName).append("-").append(company.getId()).toString());
    
    Future<OperationStatusResponse> response = azureClient.createSystemVM(name);
    Logger.info("Submited request to create system vm");
    while (!response.isDone()) {
      System.out.print('.');
      Thread.sleep(3000);
    }
    OperationStatusResponse status = response.get();
    
    RoleInstance vm = azureClient.getVirutalMachineByName(name);
    
    VMModel vmModel = VMHelper.getVMByName(name);
    vmModel.put("public_ip", vm.getIPAddress().getHostAddress());
    VMHelper.updateVM(vmModel);
    
    Logger.info("Create system vm " + name + " has been " + status.getStatus());
    
    List<OfferingModel> list = OfferingHelper.getEnableOfferings();
    Collections.sort(list, new Comparator<OfferingModel>() {
      @Override
      public int compare(OfferingModel o1, OfferingModel o2) {
        return o2.getMemory() - o1.getMemory();
      }
    });

    OfferingModel defaultOffering = list.get(0);
    OfferingHelper.addDefaultOfferingForGroup(company.getId(), defaultOffering.getId());
    
    //add to reverse proxy 
    final String vmSystemName = name;
    final String vmSystemIp = vm.getIPAddress().getHostAddress();
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {       
        
        try {
          
        //add guacamole to reverse proxy   
          
          Logger.debug("VMName:" + vmSystemName + " ip:" + vmSystemIp );
          Session session = SSHClient.getSession("127.0.0.1", 22, VMHelper.getSystemProperty("default-user"), VMHelper.getSystemProperty("default-password"));
          ChannelExec channel = (ChannelExec) session.openChannel("exec");
          
          String command = "sudo -S -p '' /etc/nginx/sites-available/manage_location.sh "
              + vmSystemIp + " " + vmSystemName + " 0";
          Logger.info("Command add to reverse proxy:" + command);    
          channel.setCommand(command);
          OutputStream out = channel.getOutputStream();
          channel.connect();

          out.write((VMHelper.getSystemProperty("default-password") + "\n").getBytes());
          out.flush();
          channel.disconnect();
          
          
          //restart service nginx
          channel = (ChannelExec) session.openChannel("exec");
          command = "sudo -S -p '' service nginx restart";              
          Logger.info("Command restart service nginx:" + command);    
          channel.setCommand(command);
          out = channel.getOutputStream();
          channel.connect();

          out.write((VMHelper.getSystemProperty("default-password") + "\n").getBytes());
          out.flush();
          SSHClient.printOut(System.out, channel);
          channel.disconnect();
          
          
         //add jenkin to reverse proxy
          
          if (SSHClient.checkEstablished(vmSystemIp, 22, 300)) {
            session = SSHClient.getSession(vmSystemIp, 22, VMHelper.getSystemProperty("default-user"), VMHelper.getSystemProperty("default-password"));
            channel = (ChannelExec) session.openChannel("exec");
            
            channel = (ChannelExec) session.openChannel("exec");
            command = "sudo -S -p '' /etc/guacamole/change_prefix_jenkin.sh " + vmSystemName;
            Logger.info("Command add to reverse proxy:" + command);    
            channel.setCommand(command);
            out = channel.getOutputStream();
            channel.connect();

            out.write((VMHelper.getSystemProperty("default-password") + "\n").getBytes());
            out.flush();
            SSHClient.printOut(System.out, channel);
            channel.disconnect();
            
            //restart jenkins service
            channel = (ChannelExec) session.openChannel("exec");
            command = "sudo -S -p '' service jenkins restart";                
            Logger.info("Command  restart service jenkins:" + command);    
            channel.setCommand(command);
            out = channel.getOutputStream();
            channel.connect();

            out.write((VMHelper.getSystemProperty("default-password") + "\n").getBytes());
            out.flush();
            SSHClient.printOut(System.out, channel);
            channel.disconnect();
 
          }          
          session.disconnect();         
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });
    thread.start();
    
  }
  
  public static void destroyVM(VMModel vm) throws Exception {

    String vmId = vm.getId();
    
    //remote node of chef server
    VMModel jenkins = VMHelper.getVMsByGroupID(vm.getGroup().getId(), new BasicDBObject("jenkins", true)).get(0);
    VMHelper.getKnife(jenkins).deleteNode(vm.getName());

    //remove node of jenkins server
    try {
      String subfix = jenkins.getName() + "/jenkins";
      new JenkinsSlave( new JenkinsMaster(jenkins.getPublicIP(), "http", subfix, 8080), vm.getPublicIP()).release();
    } catch (IOException e) {
      Logger.debug("Could not release jenkins node ", e);
    }

    //remove node from guacamole
    boolean isGui = vm.getBoolean("gui");
    Session session = SSHClient.getSession(jenkins.getPublicIP(), 22, jenkins.getUsername(), jenkins.getPassword());
    ChannelExec channel = (ChannelExec) session.openChannel("exec");
    String command = "";
    if (isGui) {
      try {
        channel = (ChannelExec) session.openChannel("exec");
        command = "sudo -S -p '' /etc/guacamole/manage_con.sh "
            + vm.getPublicIP() + " 5900 '"
            + VMHelper.getSystemProperty("default-password") + "' vnc 1";
        channel.setCommand(command);
        OutputStream out = channel.getOutputStream();
        channel.connect();

        out.write((jenkins.getPassword() + "\n").getBytes());
        out.flush();
        channel.disconnect();
      } catch (Exception e) {
        Logger.error("Exception when run:" + e.getMessage());
      }

      Logger.info("Command run add connection guacamole: " + command);

    } else {

      try {
        channel = (ChannelExec) session.openChannel("exec");
        command = "sudo -S -p '' /etc/guacamole/manage_con.sh "
            + vm.getPublicIP() + " 22 '"
            + VMHelper.getSystemProperty("default-password") + "' ssh 1";
        channel.setCommand(command);
        OutputStream out = channel.getOutputStream();
        channel.connect();

        out.write((jenkins.getPassword() + "\n").getBytes());
        out.flush();
        channel.disconnect();
      } catch (Exception e) {
        Logger.error("Exception when run:" + e.getMessage());
      }

      Logger.info("Command run add connection guacamole: " + command);

    }

    //delete virtual machine from azure
    AzureClient azureClient = VMHelper.getAzureClient();
    azureClient.deleteVirtualMachineByName(vmId);
    Logger.info("Destroying vm " + vmId);
  }
  
  public static String createNormalGuiVM(Group company) throws Exception {
    return createNormalVM(company, "Gui", "cats-ui-image");
  }
  
  public static String createNormalNonGuiVM(Group company) throws Exception {
    return createNormalVM(company, "Non-Gui", "cats-non-ui-image");
  }
  
  public static Map<String, ConcurrentLinkedQueue<String>> QueueHolder = new HashMap<String, ConcurrentLinkedQueue<String>>();

  public static String createNormalVM(Group company, final String subfix, String template, final String ...recipes) throws Exception {

    AzureClient azureClient = VMHelper.getAzureClient();
    
    OfferingModel offering = OfferingHelper.getDefaultOfferingOfGroup(company.getId()).getOffering();
    final VMModel jenkins = VMHelper.getVMsByGroupID(company.getId(), new BasicDBObject("system", true).append("jenkins", true)).get(0);
    
    //create instance
    String normalName = getAvailableName(company, subfix, 0);
    final String name = normalizeVMName(new StringBuilder(normalName).append("-").append(company.getId()).toString());
    QueueHolder.put(name, new ConcurrentLinkedQueue<String>());
    
    Future<OperationStatusResponse> response = azureClient.createVM(name, template, offering.getId());
    Logger.info("Submited request to create test vm");
    while (!response.isDone()) {
      System.out.print('.');
      Thread.sleep(3000);
    }
    
    OperationStatusResponse status = response.get();
    Logger.info("Create " + subfix + " vm " + name + " has been " + status.getStatus());    
    
    //get vm by name
    final RoleInstance vm = azureClient.getVirutalMachineByName(name);
    VMModel vmModel = VMHelper.getVMByName(name);
    if (vmModel == null) {
      vmModel = new VMModel(name, name, company.getId(), template, template, 
          vm.getIPAddress().getHostAddress(), VMHelper.getSystemProperty("default-user"), VMHelper.getSystemProperty("default-password"));
      vmModel.put("gui", "Non-Gui".equals(subfix) ? false : true);
      vmModel.put("system", false);
      vmModel.put("offering_id", offering.getId());
      vmModel.setStatus(VMStatus.Initializing);
      vmModel.put("normal_name", normalName);
      VMHelper.createVM(vmModel);
    } else {
      vmModel.put("public_ip", vm.getIPAddress().getHostAddress());
      VMHelper.updateVM(vmModel);
    }

    //Run recipes
    Thread thread = new Thread() {
      @Override
      public void run() {
        ConcurrentLinkedQueue<String> queue = QueueHolder.get(name);
        String ip = vm.getIPAddress().getHostAddress();
        try {
          queue.add("Checking SSHD on " + ip);
          if (SSHClient.checkEstablished(ip, 22, 300)) {
            
            queue.add("Connection is established");
            Session session = SSHClient.getSession(ip, 22, VMHelper.getSystemProperty("default-user"), VMHelper.getSystemProperty("default-password"));
            
            //sudo
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            //create jenkins slave
            Logger.debug("Ip Jenkins Master: "+jenkins.getPublicIP());
            String jenkins_subfix = jenkins.getName() + "/jenkins";
            JenkinsMaster master = new JenkinsMaster(jenkins.getPublicIP(), "http", jenkins_subfix, 8080);           
            Logger.debug("Ip new vm: " + ip);
            
            Map<String, String> env = new HashMap<String, String>();
            if ("Gui".equals(subfix)) env.put("DISPLAY", ":0");
            JenkinsSlave slave = new JenkinsSlave(master, ip, env);

            if (slave.join()) {
              queue.add("Create slave " + ip + " sucessfully");
            } else {
              queue.add("Can not create slave" + ip);
            }
            
            //run Jmeter
            if("Non-Gui".equals(subfix)) {
              String command = "nohup jmeter-start > jmeter-server.log 2>&1 &";
              channel.setCommand(command);
              channel.connect();            
              queue.add("Execute command: " + command);
              SSHClient.printOut(queue, channel);
              channel.disconnect();
            }
            
            //Point jenkins ip to cloud-ats.cloudapp.net
            channel = (ChannelExec) session.openChannel("exec");
            String command = "sed 's/127.0.1.1/" + jenkins.getPublicIP() + "/' /etc/hosts > /tmp/hosts";
            channel.setCommand(command);
            channel.connect();
            queue.add("Executed command: " + command);
            channel.disconnect();
            
            //replace /etc/hosts
            channel = (ChannelExec) session.openChannel("exec");
            command = "sudo -S -p '' cp /tmp/hosts /etc/hosts";
            channel.setCommand(command);
            OutputStream out = channel.getOutputStream();
            channel.connect();
            out.write((VMHelper.getSystemProperty("default-password") + "\n").getBytes());
            out.flush();
            
            //disconnect session
            session.disconnect();
            
            Logger.debug("Starting bootstrap node");
            Knife knife = VMHelper.getKnife(jenkins);
            knife.bootstrap(ip, name, VMHelper.getSystemProperty("default-user"), VMHelper.getSystemProperty("default-password"), queue, recipes);
            
            //register guacamole
            if("Gui".equals(subfix)){
              command = "sudo -S -p '' /etc/guacamole/manage_con.sh " 
                  + ip + " 5900 '"+VMHelper.getSystemProperty("default-password") + "' vnc 0";                
            } else if ("Non-Gui".equals(subfix)) {
              command = "sudo -S -p '' /etc/guacamole/manage_con.sh " 
                  + ip + " 22 '"+VMHelper.getSystemProperty("default-password") + "' ssh 0";             
            }
            
            session = SSHClient.getSession(jenkins.getPublicIP(), 22, jenkins.getUsername(), jenkins.getPassword());              
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            out = channel.getOutputStream();
            channel.connect();

            out.write((VMHelper.getSystemProperty("default-password") + "\n").getBytes());
            out.flush();
            channel.disconnect();
            session.disconnect();
            queue.add("Command run add connection guacamole: "+ command);
            
            //Update vm status to Ready
            VMModel vmModel = VMHelper.getVMByName(name);
            vmModel.setStatus(VMStatus.Ready);
            VMHelper.updateVM(vmModel);
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
    
    //
    thread.start();
    return name;
  }
  
  public static String getAvailableName(Group company, String subfix, int index) throws Exception {
    AzureClient azureClient = VMHelper.getAzureClient();    
    
    String normalName = new StringBuilder(company.getString("name")).append("-").append(subfix).append("-").append(index).toString();
    String name = normalizeVMName(new StringBuilder(normalName).append("-").append(company.getId()).toString());    
    RoleInstance vm = azureClient.getVirutalMachineByName(name);
    if (vm == null) return normalName;
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
