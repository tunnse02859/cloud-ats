/**
 * 
 */
package controllers;

import interceptor.AuthenticationInterceptor;
import interceptor.WizardInterceptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import models.vm.OfferingModel;
import models.vm.VMModel;

import org.apache.cloudstack.api.ApiConstants.VMDetails;
import org.apache.cloudstack.jobs.JobInfo.Status;
import org.ats.cloudstack.AsyncJobAPI;
import org.ats.cloudstack.CloudStackClient;
import org.ats.cloudstack.VirtualMachineAPI;
import org.ats.cloudstack.model.Job;
import org.ats.cloudstack.model.VirtualMachine;
import org.ats.common.ssh.SSHClient;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.feature.OperationDAO;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.mongodb.BasicDBObject;

import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import utils.LogBuilder;
import utils.OfferingHelper;
import utils.VMHelper;
import views.html.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 1, 2014
 */
@With(WizardInterceptor.class)
public class Application extends Controller {

  public static Result index() {
    return ok(index.render());
  }
  
  public static Result signup(boolean group) {
    return ok(signup.render(group));
  }
  
  public static Result doSignup() throws UserManagementException, IOException, JSchException {
    DynamicForm form = Form.form().bindFromRequest();
    boolean group = Boolean.parseBoolean(form.get("group"));
    
    if (group) {
      String companyName = form.get("company");
      String companyHost = form.get("host");
      Group company = new Group(companyName);
      company.put("host", companyHost);
      company.put("level", 1);
      
      String adminEmail = form.get("email");
      String adminPassword = form.get("password");
      
      User admin = new User(adminEmail, adminEmail);
      admin.put("password", adminPassword);
      company.addUser(admin.getId());
      admin.joinGroup(company);
      admin.put("joined", true);
      
      Feature organization = FeatureDAO.INSTANCE.find(new BasicDBObject("name", "Organization")).iterator().next();
      company.addFeature(organization);  
      
      Role administration = new Role("Administration", company.getId());
      administration.put("desc", "This is administration role for organization management");
      administration.put("system", true);
      company.addRole(administration);
      
      for (Operation operation : organization.getOperations()) {
        administration.addPermission(new Permission(organization.getId(), operation.getId()));
      }
      administration.addUser(admin);
      admin.addRole(administration);
      
      Feature vmFeature = FeatureDAO.INSTANCE.find(new BasicDBObject("name", "Virtual Machine")).iterator().next();
      company.addFeature(vmFeature);
      
      Operation sysMgt = OperationDAO.INSANCE.find(new BasicDBObject("name", "Manage System VM")).iterator().next();
      Operation normalMgt = OperationDAO.INSANCE.find(new BasicDBObject("name", "Manage Normal VM")).iterator().next();
      
      Role vmRole = new Role("VM Management", company.getId());
      
      vmRole.addPermission(new Permission(vmFeature.getId(), sysMgt.getId()));
      vmRole.addPermission(new Permission(vmFeature.getId(), normalMgt.getId()));

      vmRole.addUser(admin);
      admin.addRole(vmRole);
      company.addRole(vmRole);
      
      GroupDAO.INSTANCE.create(company);
      UserDAO.INSTANCE.create(admin);
      RoleDAO.INSTANCE.create(administration, vmRole);
      
      //Create system vm 
      createCompanyVM(company);
      
      session().clear();
      session().put("email", admin.getEmail());
      session().put("user_id", admin.getId());
      session().put("group_id", company.getId());
    } else {
      String email = form.get("email");
      String password = form.get("password");
      User user = new User(email, email);
      user.put("password", password);
      UserDAO.INSTANCE.create(user);
      
      session().put("email", user.getEmail());
      session().put("user_id", user.getId());
    }
    return redirect(controllers.routes.Application.dashboard());
  }
  
  private static void createCompanyVM(Group company) throws IOException, JSchException {
    CloudStackClient client = VMHelper.getCloudStackClient();
    String[] response = VirtualMachineAPI.quickDeployVirtualMachine(client, company.getString("name") + "-jenkins", "gitlab-jenkins", "Large Instance", null);
    String vmId = response[0];
    String jobId = response[1];
    Job job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
    while (!job.getStatus().done()) {
      job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
    }
    
    if (job.getStatus() == org.apache.cloudstack.jobs.JobInfo.Status.SUCCEEDED) {
      
      VirtualMachine vm = VirtualMachineAPI.findVMById(client, vmId, null);
      VMModel vmModel = new VMModel(vm.id, vm.name, company.getId(), vm.templateName, vm.templateId, vm.nic[0].ipAddress, "ubuntu", "ubuntu");
      vmModel.put("system", true);
      vmModel.put("offering_id", vm.serviceOfferingId);

      //edit `/etc/hosts` file
      StringBuilder sb = new StringBuilder();
      LogBuilder.log(sb, "Checking SSHD on " + vmModel.getPublicIP());
      if (SSHClient.checkEstablished(vmModel.getPublicIP(), 22, 120)) {
        LogBuilder.log(sb, "Connection is established");
        
        Session session = SSHClient.getSession(vmModel.getPublicIP(), 22, vmModel.getUsername(), vmModel.getPassword());
        
        //sudo
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        String command = "sed 's/127.0.1.1/" + vmModel.getPublicIP() + "/' /etc/hosts > /tmp/hosts";
        channel.setCommand(command);
        channel.connect();
        LogBuilder.log(sb, "Execute command: " + command);
        channel.disconnect();
        
        //replace hosts
        channel = (ChannelExec) session.openChannel("exec");
        command = "sudo -S -p '' cp /tmp/hosts /etc/hosts";
        channel.setCommand(command);
        OutputStream out = channel.getOutputStream();
        channel.connect();
        
        out.write((vmModel.getPassword() + "\n").getBytes());
        out.flush();
        
        LinkedList<String> queue = new LinkedList<String>();
        int exitCode = SSHClient.printOut(queue, channel);
        LogBuilder.log(sb, "Execute command: " + command);
        
        for (String s : queue) {
          LogBuilder.log(sb, s);
        }
        LogBuilder.log(sb, "exit code: " + exitCode);
        channel.disconnect();
        
        //start jenkins
        channel = (ChannelExec) session.openChannel("exec");
        command = "/home/ubuntu/java/jdk1.7.0_51/bin/java -jar jenkins.war > log.txt &";
        channel.setCommand(command);
        channel.connect();
        LogBuilder.log(sb, "Execute command: " + command);
        
        exitCode = SSHClient.printOut(queue, channel);
        
        for (String s : queue) {
          LogBuilder.log(sb, s);
        }
        LogBuilder.log(sb, "exit code: " + exitCode);
        channel.disconnect();
        
        //disconnect session
        session.disconnect();
      } else {
        LogBuilder.log(sb, "Cloud not establish connection in 120s");
      }
      
      vmModel.put("log", sb.toString());
      
      VMHelper.createVM(vmModel);
      
      List<OfferingModel> list = OfferingHelper.getEnableOfferings();
      Collections.sort(list, new Comparator<OfferingModel>() {
        @Override
        public int compare(OfferingModel o1, OfferingModel o2) {
          return o1.getMemory() - o2.getMemory();
        }
      });
      
      OfferingModel defaultOffering = list.get(0);
      OfferingHelper.addOfferingGroup(company.getId(), defaultOffering);
      
    } else {
      Logger.error("Could not create system vm for company " + company.getString("name"));
      createCompanyVM(company);
    }
  }
  
  public static Result signin() {
    return ok(signin.render());
  }
  
  public static Result doSignin() throws UserManagementException {
    DynamicForm form = Form.form().bindFromRequest();
    String email = form.get("email");
    String password = form.get("password");
    Collection<User> users = UserDAO.INSTANCE.find(new BasicDBObject("email", email));
    
    if (users.isEmpty() || users.size() > 1) {
      flash().put("signin-faild", "true");
      return redirect(controllers.routes.Application.signin());
    }
    
    User user = users.iterator().next();
    if (user.getString("password").equals(password) && user.getBoolean("active")) {
      session().put("email", user.getEmail());
      session().put("user_id", user.getId());
      return redirect(controllers.routes.Application.dashboard());
    }
    
    flash().put("signin-faild", "true");
    return redirect(controllers.routes.Application.signin());
  }
  
  public static Result signout() {
    session().clear();
    return redirect(controllers.routes.Application.index());
  }
  
  public static Result untrail(String path) {
    return movedPermanently("/" + path);
  }
  
  @With(AuthenticationInterceptor.class)
  public static Result dashboard() {
    return ok(views.html.dashboard.dashboard.render());
  }
  
  public static Result wizard() throws UserManagementException {
    return ok(views.html.wizard.render());
  }
  
  public static Result doWizard() throws UserManagementException {
    session().clear();
    
    DynamicForm form = Form.form().bindFromRequest();
    String email = form.get("email");
    String password = form.get("password");
    
    Feature organization = new Feature("Organization");
    organization.put("desc", "This is organization management feature");
    organization.put("system", true);
    
    Operation ad = new Operation("Administration");
    organization.addOperation(ad);
    
    Group system = new Group("System Admin");
    system.put("desc", "This is group of system");
    system.put("system", true);
    system.put("level", 0);
    system.addFeature(organization);
    
    User root = new User(email, email);
    root.put("system", true);
    root.put("password", password);
    root.joinGroup(system);
    root.put("joined", true);
    
    system.addUser(root);
    
    Role administration = new Role("Administration", system.getId());
    administration.put("desc", "This is administration role for organization management");
    administration.put("system", true);
    administration.addPermission(new Permission(organization.getId(), ad.getId()));
    administration.addUser(root);
    root.addRole(administration);
    system.addRole(administration);

    //persist
    FeatureDAO.INSTANCE.create(organization);
    OperationDAO.INSANCE.create(ad);
    UserDAO.INSTANCE.create(root);
    GroupDAO.INSTANCE.create(system);
    RoleDAO.INSTANCE.create(administration);
    
    createVMFeature(root, system);
    
    //login
    session().clear();
    session().put("email", root.getEmail());
    session().put("user_id", root.getId());
    session().put("group_id", system.getId());
    
    return redirect(controllers.vm.routes.VMController.index());
  }
  
  private static void createVMFeature(User rootUser, Group systemGroup) throws UserManagementException {
    Feature feature = new Feature("Virtual Machine");
    
    Operation o1 = new Operation("Manage System VM");
    Operation o2 = new Operation("Manage Normal VM");
    
    feature.addOperation(o1);
    feature.addOperation(o2);
    
    Role vmRole = new Role("VM Management", systemGroup.getId());
    vmRole.addPermission(new Permission(feature.getId(), o1.getId()));
    vmRole.addPermission(new Permission(feature.getId(), o2.getId()));
    vmRole.addUser(rootUser);
    
    rootUser.addRole(vmRole);
    
    systemGroup.addFeature(feature);
    systemGroup.addRole(vmRole);
    
    FeatureDAO.INSTANCE.create(feature);
    OperationDAO.INSANCE.create(o1, o2);
    RoleDAO.INSTANCE.create(vmRole);
    
    UserDAO.INSTANCE.update(rootUser);
    GroupDAO.INSTANCE.update(systemGroup);
  }
}
