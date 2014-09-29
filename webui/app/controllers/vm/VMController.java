/**
 * 
 */
package controllers.vm;

import static akka.pattern.Patterns.ask;
import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.VMWizardIterceptor;
import interceptor.WithSystem;
import interceptor.WizardInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;

import models.vm.DefaultOfferingModel;
import models.vm.OfferingModel;
import models.vm.VMModel;

import org.apache.cloudstack.api.ApiConstants.VMDetails;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ats.cloudstack.AsyncJobAPI;
import org.ats.cloudstack.CloudStackAPI;
import org.ats.cloudstack.CloudStackClient;
import org.ats.cloudstack.ServiceOfferingAPI;
import org.ats.cloudstack.TemplateAPI;
import org.ats.cloudstack.VirtualMachineAPI;
import org.ats.cloudstack.model.Job;
import org.ats.cloudstack.model.ServiceOffering;
import org.ats.cloudstack.model.Template;
import org.ats.cloudstack.model.VirtualMachine;
import org.ats.common.html.HtmlParser;
import org.ats.common.html.XPathUtil;
import org.ats.common.http.HttpClientUtil;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;
import org.ats.jenkins.JenkinsMaster;
import org.ats.jenkins.JenkinsSlave;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import play.api.templates.Html;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.F.Function;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.mvc.With;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import utils.OfferingHelper;
import utils.VMHelper;
import views.html.vm.alert;
import views.html.vm.index;
import views.html.vm.offering;
import views.html.vm.offeringbody;
import views.html.vm.propertiesbody;
import views.html.vm.terminal;
import views.html.vm.vmbody;
import views.html.vm.vmproperties;
import views.html.vm.vmstatus;
import views.html.vm.wizard;

import com.cloud.template.VirtualMachineTemplate.TemplateFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;

import controllers.organization.Organization;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 4, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Virtual Machine", operation = "")
public class VMController extends Controller {

  @With(VMWizardIterceptor.class)
  public static Result index() throws Exception {
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));

    if (VMHelper.vmCount() == 0) {
      return currentUser.getBoolean("system") ? ok(index.render(wizard.render())) : forbidden(views.html.forbidden.render());
    }

    boolean system = Organization.isSystem(currentUser);
    Group group = null;
    if (system) {
      group = GroupDAO.INSTANCE.find(new BasicDBObject("system", true)).iterator().next();
    } else {
      BasicDBObject query = new BasicDBObject("level", 1);
      query.append("user_ids", Pattern.compile(currentUser.getId()));
      group = GroupDAO.INSTANCE.find(query).iterator().next();
    }
    session("group_id", group.getId());
    if (hasPermission(group, "Manage System VM")) {
      Html html = vmbody.render(system, group, VMHelper.getVMsByGroupID(group.getId(), new BasicDBObject("system", true)), false);
      return ok(index.render(html));
    } else {
      return redirect(routes.VMController.normalVMView(group.getId()));
    }
  }

  @With(VMWizardIterceptor.class)
  public static WebSocket<JsonNode> vmStatus(final String groupId, final String sessionId) {
    return new WebSocket<JsonNode>() {
      @Override
      public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
        try {
          Await.result(ask(VMStatusActor.actor, new VMChannel(sessionId, groupId, out), 1000), Duration.create(1, TimeUnit.SECONDS));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
  }
  
  @With(VMWizardIterceptor.class)
  public static WebSocket<JsonNode> vmLog(final String groupId, final String sessionId) {
    return new WebSocket<JsonNode>() {
      @Override
      public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
        try {
          Await.result(ask(VMLogActor.actor, new VMChannel(sessionId, groupId, out), 1000), Duration.create(1, TimeUnit.SECONDS));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
  }

  @With(VMWizardIterceptor.class)
  @Authorization(feature = "Virtual Machine", operation = "Manage System VM")
  public static Result offeringView() throws Exception {
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    boolean system = Organization.isSystem(currentUser);

    Group group = null;
    if (system) {
      group = GroupDAO.INSTANCE.find(new BasicDBObject("system", true)).iterator().next();
    } else {
      BasicDBObject query = new BasicDBObject("level", 1);
      query.append("user_ids", Pattern.compile(currentUser.getId()));
      group = GroupDAO.INSTANCE.find(query).iterator().next();
    }
    List<OfferingModel> list = system ? OfferingHelper.getOfferings() : OfferingHelper.getEnableOfferings();
    Html html = offeringbody.render(system, group, list);
    return ok(index.render(html));
  }

  @WithSystem
  public static Result getOfferings() throws Exception {
    String cloudstackApiUrl = request().getQueryString("cloudstack-api-url");
    String cloudstackApiKey = request().getQueryString("cloudstack-api-key");
    String cloudstackApiSecret = request().getQueryString("cloudstack-api-secret");
    CloudStackClient client = new CloudStackClient(cloudstackApiUrl, cloudstackApiKey, cloudstackApiSecret);

    List<ServiceOffering> list = ServiceOfferingAPI.listServiceOfferings(client, null, null);
    return ok(offering.render(list));
  }

  @WithSystem
  @With(VMWizardIterceptor.class)
  @Authorization(feature = "Virtual Machine", operation = "Manage System VM")
  public static Result propertiesView() throws Exception {
    Group group = GroupDAO.INSTANCE.find(new BasicDBObject("system", true)).iterator().next();
    return ok(index.render(propertiesbody.render(checkCurrentSystem(), group, VMHelper.getSystemProperties())));
  }
  
  @WithSystem
  @With(VMWizardIterceptor.class)
  @Authorization(feature = "Virtual Machine", operation = "Manage System VM")
  public static Result saveProperties() throws Exception {
    DynamicForm form = Form.form().bindFromRequest();
    String cloudstackApiUrl = form.get("cloudstack-api-url");
    String cloudstackApiKey = form.get("cloudstack-api-key");
    String cloudstackApiSecret = form.get("cloudstack-api-secret");
    String cloudstackUsername = form.get("cloudstack-username");
    String cloudstackPassword = form.get("cloudstack-password");
    
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("cloudstack-api-url", cloudstackApiUrl);
    properties.put("cloudstack-api-key", cloudstackApiKey);
    properties.put("cloudstack-api-secret", cloudstackApiSecret);
    properties.put("cloudstack-username", cloudstackUsername);
    properties.put("cloudstack-password", cloudstackPassword);

    VMHelper.updateSystemProperties(properties);
    return redirect(routes.VMController.propertiesView());
  }

  @WithSystem
  public static Result doWizard() throws UserManagementException, IOException {
    DynamicForm form = Form.form().bindFromRequest();

    Group systemGroup = GroupDAO.INSTANCE.find(new BasicDBObject("system", true)).iterator().next();

    String cloudstackApiUrl = form.get("cloudstack-api-url");
    String cloudstackApiKey = form.get("cloudstack-api-key");
    String cloudstackApiSecret = form.get("cloudstack-api-secret");
    String cloudstackUsername = form.get("cloudstack-username");
    String cloudstackPassword = form.get("cloudstack-password");

    CloudStackClient client = new CloudStackClient(cloudstackApiUrl, cloudstackApiKey, cloudstackApiSecret);

    String jenkinsIP = form.get("jenkins-ip");
    String jenkinsUsername = form.get("jenkins-username");
    String jenkinsPassword = form.get("jenkins-password");

    Template template = TemplateAPI.listTemplates(client, TemplateFilter.all, null, "gitlab-jenkins",  null).get(0);
    VirtualMachine vm = VirtualMachineAPI.listVirtualMachines(client, null, null, null, template.id, null).get(0);
    VMModel jenkinsVM = new VMModel(vm.id, "system-jenkins", systemGroup.getId(), vm.templateName, vm.templateId, jenkinsIP, jenkinsUsername, jenkinsPassword);
    jenkinsVM.put("system", true);
    jenkinsVM.put("jenkins", true);
    jenkinsVM.put("offering_id", vm.serviceOfferingId);

    String chefServerIp = form.get("chef-server-ip");
    String chefWorkstationIp = form.get("chef-workstation-ip");
    String chefUsername = form.get("chef-username");
    String chefPassword = form.get("chef-password");

    template = TemplateAPI.listTemplates(client, TemplateFilter.all, null, "chef-server",  null).get(0);
    vm = VirtualMachineAPI.listVirtualMachines(client, null, null, null, template.id, null).get(0);
    VMModel chefServerVM = new VMModel(vm.id, "chef-server", systemGroup.getId(), vm.templateName, vm.templateId, chefServerIp, chefUsername, chefPassword);
    chefServerVM.put("system", true);
    chefServerVM.put("offering_id", vm.serviceOfferingId);

    template = TemplateAPI.listTemplates(client, TemplateFilter.all, null, "chef-workstation",  null).get(0);
    vm = VirtualMachineAPI.listVirtualMachines(client, null, null, null, template.id, null).get(0);
    VMModel chefWorkstationVM = new VMModel(vm.id, "chef-workstation", systemGroup.getId(), vm.templateName, vm.templateId, chefWorkstationIp, chefUsername, chefPassword);
    chefWorkstationVM.put("system", true);
    chefWorkstationVM.put("offering_id", vm.serviceOfferingId);

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("cloudstack-api-url", cloudstackApiUrl);
    properties.put("cloudstack-api-key", cloudstackApiKey);
    properties.put("cloudstack-api-secret", cloudstackApiSecret);
    properties.put("cloudstack-username", cloudstackUsername);
    properties.put("cloudstack-password", cloudstackPassword);

    VMHelper.setSystemProperties(properties);
    VMHelper.createVM(jenkinsVM, chefServerVM, chefWorkstationVM);

    createOffering(form, client);

    return redirect(controllers.vm.routes.VMController.index());
  }

  private static void createOffering(DynamicForm form, CloudStackClient client) throws IOException {
    List<ServiceOffering> list = ServiceOfferingAPI.listServiceOfferings(client, null, null);
    for (ServiceOffering offering : list) {
      OfferingModel model = new OfferingModel(offering.id, offering.name, offering.cpuNumber, offering.cpuSpeed, offering.memory);
      model.put("disabled", form.get("offering-" + offering.id) != null ? false : true);
      OfferingHelper.createOffering(model);
    }
  }

  @With(VMWizardIterceptor.class)
  public static Result viewConsoleURL(String vmId) {
    String cloudstackApiUrl = VMHelper.getSystemProperty("cloudstack-api-url");
    String cloudstackUsername = VMHelper.getSystemProperty("cloudstack-username");
    String cloudstackPassword = VMHelper.getSystemProperty("cloudstack-password");

    try {
      DefaultHttpClient client = CloudStackAPI.login(VMHelper.getCloudStackClient(), cloudstackUsername, cloudstackPassword);
      String cloudstackConsoleUrl = cloudstackApiUrl.substring(0, cloudstackApiUrl.lastIndexOf('/') + 1) + "console?cmd=access&vm=" + vmId;
      String response = HttpClientUtil.fetch(client, cloudstackConsoleUrl);

      HtmlParser parser = new HtmlParser();
      Document doc = parser.parseWellForm(response);
      NodeList nodeList = (NodeList)XPathUtil.read(doc, "/html/frameset/frame", XPathConstants.NODESET);
      Node node = nodeList.item(0);
      response().setContentType("html");
      String src = node.getAttributes().getNamedItem("src").getNodeValue();
      return ok(terminal.render(src));
    } catch (Exception e) {
      e.printStackTrace();
      return ok();
    }
  }

  /**
   * Start, Stop or Restore VM
   * @param action
   * @param vmId
   * @return
   * @throws Exception
   */
  @With(VMWizardIterceptor.class)
  public static Promise<Result> vmAction(String action, final String vmId) throws Exception {

    if (! hasRightPermission(vmId)) return Promise.<Result>pure(forbidden(views.html.forbidden.render()));

    final CloudStackClient client = VMHelper.getCloudStackClient();

    if ("start".equals(action)) {
      String jobId =VirtualMachineAPI.startVM(client, vmId);
      Job job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
      while (!job.getStatus().done()) {
        job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
      }

      if (job.getStatus() == org.apache.cloudstack.jobs.JobInfo.Status.SUCCEEDED) {
        VMModel vm = VMHelper.getVMByID(vmId);
        if (vm.getBoolean("jenkins")) VMCreator.startJenkins(vm);
      }
    } else if ("stop".equals(action)) {
      VirtualMachineAPI.stopVM(client, vmId, false);
    } else if ("restore".equals(action)) {
      VirtualMachineAPI.restoreVM(client, vmId, null);
    } else if ("destroy".equals(action)) {
      final VMModel vm = VMHelper.getVMByID(vmId);

      if (!hasPermission(vm.getGroup(), "Manage Normal VM")) return Promise.<Result>pure(forbidden(views.html.forbidden.render()));

      Promise<Boolean> result = Promise.promise(new Function0<Boolean>() {
        @Override
        public Boolean apply() throws Throwable {
          String jobId = VirtualMachineAPI.destroyVM(client, vmId, true);
          Job job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
          while (!job.getStatus().done()) {
            job = AsyncJobAPI.queryAsyncJobResult(client, jobId);
          }

          if (job.getStatus() == org.apache.cloudstack.jobs.JobInfo.Status.SUCCEEDED) {
            VMHelper.removeVM(vm);
            
            VMModel jenkins = VMHelper.getVMsByGroupID(vm.getGroup().getId(), new BasicDBObject("jenkins", true)).get(0);
            VMHelper.getKnife().deleteNode(vm.getName());
            new JenkinsSlave(new JenkinsMaster(jenkins.getPublicIP(), "http", 8080), vm.getPublicIP()).release();
            
            return true;
          }
          return false;
        }
      });
      return result.map(new Function<Boolean, Result>() {
        @Override
        public Result apply(Boolean a) throws Throwable {
          return a ? status(200) : status(500);
        }
      });
    }

    return Promise.<Result>pure(status(200));
  }

  @WithSystem
  @With(VMWizardIterceptor.class)
  public static Result updateVMProperties() throws Exception {
    DynamicForm form = Form.form().bindFromRequest();

    CloudStackClient client = VMHelper.getCloudStackClient();
    VMModel vmModel = VMHelper.getVMByID(form.get("vmId"));

    String ip = form.get("ip");
    String username = form.get("username");
    String password = form.get("password");

    ObjectNode json = Json.newObject();

    if (vmModel.getPublicIP().equals(ip)) {
      vmModel.put("username", username);
      vmModel.put("password", password);
      VMHelper.updateVM(vmModel);

      json.put("vmStatus", vmstatus.render(vmModel, true).toString());
      json.put("vmProperties", vmproperties.render(vmModel, true, null, true).toString());
      return ok(json);
    }

    List<VirtualMachine> vms = VirtualMachineAPI.listVirtualMachines(client, null, null, null, vmModel.getTemplateId(), VMDetails.nics);
    for (VirtualMachine vm : vms) {
      if (ip.equals(vm.nic[0].ipAddress)) {
        VMModel existedVMModel = VMHelper.getVMByID(vm.id);
        if (existedVMModel != null) {
          json.put("vmStatus", vmstatus.render(vmModel, true).toString());
          json.put("vmProperties", vmproperties.render(vmModel, true, alert.render("The ip " + ip + " is in use."), true).toString());
          return ok(json);
        } else {
          VMHelper.removeVM(vmModel);
          vmModel.put("_id", vm.id);
          vmModel.put("public_ip", ip);
          vmModel.put("username", username);
          vmModel.put("password", password);
          VMHelper.createVM(vmModel);

          json.put("vmStatus", vmstatus.render(vmModel, true).toString());
          json.put("vmProperties", vmproperties.render(vmModel, true, null, true).toString());
          return ok(json);
        }
      }
    }

    json.put("vmStatus", vmstatus.render(vmModel, true).toString());
    json.put("vmProperties", vmproperties.render(vmModel, true, alert.render("The ip " + ip + " is not available."), true).toString());
    return ok(json);
  }

  private static boolean hasRightPermission(String vmId) throws Exception {

    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    VMModel vmModel = VMHelper.getVMByID(vmId);
    Group group = vmModel.getGroup();

    if (!checkCurrentSystem()) {
      if (!group.getUsers().contains(currentUser)) return false;

      if (vmModel.getBoolean("system")) {
        return hasPermission(group, "Manage System VM");
      } else {
        return hasPermission(group, "Manage Normal VM");
      }
    }

    return true;
  }

  public static boolean hasPermission(Group group, String operation) throws Exception {
    if (!checkCurrentSystem()) {

      User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));

      //check user ACL
      if (group.getInt("level") != 1 || !group.getUsers().contains(currentUser)) {
        return false;
      }

      Collection<Role> roles = RoleDAO.INSTANCE.find(new BasicDBObject("group_id", group.getId()).append("user_ids", Pattern.compile(currentUser.getId())));

      for (Role role : roles) {
        for (Permission per : role.getPermissions()) {
          Feature f = per.getFeature();
          if (f.getName().equals("Virtual Machine")) {
            Operation o = per.getOpertion();
            if (o.getName().equals(operation)) {
              return true;
            }
          }
        }
      }
      return false;
    }

    return true;
  }

  @With(VMWizardIterceptor.class)
  @Authorization(feature = "Virtual Machine", operation = "Manage Normal VM")
  public static Result normalVMView(String groupId) throws Exception {
    Group group = GroupDAO.INSTANCE.findOne(groupId);

    if (! hasPermission(group, "Manage Normal VM")) return forbidden(views.html.forbidden.render());

    List<VMModel> list = VMHelper.getVMsByGroupID(groupId, new BasicDBObject("system", false));
    Html html = vmbody.render(checkCurrentSystem(), group, list, true);
    return ok(index.render(html));
  }

  @With(VMWizardIterceptor.class)
  @Authorization(feature = "Virtual Machine", operation = "Manage Normal VM")
  public static Promise<Result> createrNormalVM(String groupId, final boolean gui) throws Exception {
    final Group company = GroupDAO.INSTANCE.findOne(groupId);
    Promise<VMModel> result = Promise.promise(new Function0<VMModel>() {
      @Override
      public VMModel apply() throws Throwable {
        VMModel vm = VMHelper.getVMsByGroupID(company.getId(), new BasicDBObject("jenkins", true)).get(0);
        JenkinsMaster jenkins = new JenkinsMaster(vm.getPublicIP(), "http", 8080);
        if (jenkins.isReady()) {
          return gui  ? VMCreator.createNormalGuiVM(company): VMCreator.createNormalNonGuiVM(company);
        }
        return null;
      }
    });

    return result.map(new Function<VMModel, Result>() {
      @Override
      public Result apply(VMModel vm) throws Throwable {
        scala.collection.mutable.StringBuilder sb = new scala.collection.mutable.StringBuilder();
        sb.append(vmstatus.render(vm, false));
        sb.append(vmproperties.render(vm, checkCurrentSystem(), null, false));
        return ok(sb.toString());
      }
    });
  }
  
  @With(VMWizardIterceptor.class)
  @Authorization(feature = "Virtual Machine", operation = "Manage System VM")
  public static Result saveOffering(String groupId) throws Exception {
    DynamicForm form = Form.form().bindFromRequest();
    if (checkCurrentSystem()) {
      List<OfferingModel> offerings = OfferingHelper.getOfferings();
      List<OfferingModel> disabledOfferings = new ArrayList<OfferingModel>();
      boolean blank = true;
      for (OfferingModel offering : offerings) {
        if (form.get("offering-" + offering.getId()) != null) {
          blank = false;
          offering.put("disabled", false);
          //OfferingHelper.updateOffering(offering);
        } else {
          offering.put("disabled",true);
          disabledOfferings.add(offering);
        }
      }
      if (!blank) {
        for (OfferingModel offering : offerings) {
          OfferingHelper.updateOffering(offering);
        }
        //check if has only one offering
        List<OfferingModel> enabledOfferings = OfferingHelper.getEnableOfferings();
        if (enabledOfferings.size() == 1) {
          List<DefaultOfferingModel> groups = OfferingHelper.getInvalidDefaultOffering();
          for (DefaultOfferingModel groupOffering : groups) {
            groupOffering.put("offering_id", enabledOfferings.get(0).getId());
            OfferingHelper.updateDefaultOfferingOfGroup(groupOffering);
          }
        }
      }
    } else {
      String offeringId = form.get("offering");
      OfferingHelper.removeDefaultOfferingOfGroup(groupId);
      OfferingHelper.addDefaultOfferingForGroup(groupId, offeringId);
    }
    return ok();
  }

  public static boolean checkCurrentSystem() throws UserManagementException {
    return Organization.isSystem(UserDAO.INSTANCE.findOne(session("user_id")));
  }
}
