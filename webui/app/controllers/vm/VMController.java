/**
 * 
 */
package controllers.vm;

import static akka.pattern.Patterns.ask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathConstants;

import models.vm.VMModel;

import org.apache.cloudstack.api.ApiConstants.VMDetails;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ats.cloudstack.CloudStackAPI;
import org.ats.cloudstack.CloudStackClient;
import org.ats.cloudstack.TemplateAPI;
import org.ats.cloudstack.VirtualMachineAPI;
import org.ats.cloudstack.model.Template;
import org.ats.cloudstack.model.VirtualMachine;
import org.ats.common.html.HtmlParser;
import org.ats.common.html.XPathUtil;
import org.ats.common.http.HttpClientUtil;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.feature.OperationDAO;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.cloud.template.VirtualMachineTemplate.TemplateFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObject;

import controllers.organization.Organization;
import controllers.vm.VMStatusActor.VMChannel;
import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WithSystem;
import interceptor.WizardInterceptor;
import play.api.templates.Html;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;
import play.mvc.With;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import utils.VMHelper;
import views.html.vm.*;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 4, 2014
 */
@With({WizardInterceptor.class, AuthenticationInterceptor.class})
@Authorization(feature = "Virtual Machine", operation = "")
public class VMController extends Controller {
  
  public static Result index() throws UserManagementException {
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    
    if (VMHelper.vmCount() == 0) {
      return currentUser.getBoolean("system") ? ok(index.render(wizard.render(), "Virtual Machine")) : forbidden(views.html.forbidden.render());
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
    
    Html html = body.render(system, group, VMHelper.getVMsByGroupID(group.getId(), new BasicDBObject("system", true)));
    return ok(index.render(html, "Virtual Machine"));
  }
  
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
    
    String chefServerIp = form.get("chef-server-ip");
    String chefWorkstationIp = form.get("chef-workstation-ip");
    String chefUsername = form.get("chef-username");
    String chefPassword = form.get("chef-password");
    
    template = TemplateAPI.listTemplates(client, TemplateFilter.all, null, "chef-server",  null).get(0);
    vm = VirtualMachineAPI.listVirtualMachines(client, null, null, null, template.id, null).get(0);
    VMModel chefServerVM = new VMModel(vm.id, "chef-server", systemGroup.getId(), vm.templateName, vm.templateId, chefServerIp, chefUsername, chefPassword);
    chefServerVM.put("system", true);
    
    template = TemplateAPI.listTemplates(client, TemplateFilter.all, null, "chef-workstation",  null).get(0);
    vm = VirtualMachineAPI.listVirtualMachines(client, null, null, null, template.id, null).get(0);
    VMModel chefWorkstationVM = new VMModel(vm.id, "chef-workstation", systemGroup.getId(), vm.templateName, vm.templateId, chefWorkstationIp, chefUsername, chefPassword);
    chefWorkstationVM.put("system", true);
    
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("cloudstack-api-url", cloudstackApiUrl);
    properties.put("cloudstack-api-key", cloudstackApiKey);
    properties.put("cloudstack-api-secret", cloudstackApiSecret);
    properties.put("cloudstack-username", cloudstackUsername);
    properties.put("cloudstack-password", cloudstackPassword);
    
    VMHelper.setSystemProperties(properties);
    VMHelper.createVM(jenkinsVM, chefServerVM, chefWorkstationVM);

    return redirect(controllers.vm.routes.VMController.index());
  }
  
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
  
  public static Result vmAction(String action, String vmId) throws Exception {

    if (! hasRightPermission(vmId)) return forbidden(views.html.forbidden.render());
    
    CloudStackClient client = VMHelper.getCloudStackClient();
    
    if ("start".equals(action)) {
      VirtualMachineAPI.startVM(client, vmId);
    } else if ("stop".equals(action)) {
      VirtualMachineAPI.stopVM(client, vmId, false);
    } else if ("restore".equals(action)) {
      VirtualMachineAPI.restoreVM(client, vmId, null);
    }
    
    return redirect(controllers.vm.routes.VMController.index());
  }
  
  @WithSystem
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
    
    if (!group.getUsers().contains(currentUser)) return false;
    if (vmModel.getBoolean("system") && ! hasPermission("Manage System VM")) return false;
    if (! vmModel.getBoolean("system") && ! hasPermission("Manage Normal VM")) return false;
    return true;
  }
  
  private static boolean hasPermission(String operation) throws Exception {
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    
    Feature vmMgt = FeatureDAO.INSTANCE.find(new BasicDBObject("name", "Virtual Machine")).iterator().next();
    Operation opt = OperationDAO.INSANCE.find(new BasicDBObject("name", operation)).iterator().next();

    for (Role role : currentUser.getRoles()) {
      for (Permission per : role.getPermissions()) {
        if (per.getFeature().equals(vmMgt) && per.getOpertion().equals(opt)) {
          return true;
        }
      }
    }
    return false;
  }
}
