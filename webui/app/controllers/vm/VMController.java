/**
 * 
 */
package controllers.vm;

import static akka.pattern.Patterns.ask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import models.vm.VMModel;

import org.ats.cloudstack.CloudStackClient;
import org.ats.cloudstack.TemplateAPI;
import org.ats.cloudstack.VirtualMachineAPI;
import org.ats.cloudstack.model.Template;
import org.ats.cloudstack.model.VirtualMachine;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.cloud.template.VirtualMachineTemplate.TemplateFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;

import controllers.organization.Organization;
import controllers.vm.VMStatusActor.VMChannel;
import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WithSystem;
import interceptor.WizardInterceptor;
import play.data.DynamicForm;
import play.data.Form;
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
    
    if (VMHelper.systemCount() == 0) {
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
    return ok(index.render(body.render(system, group, VMHelper.getVMsByGroupID(group.getId())), "Virtual Machine"));
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
    
    CloudStackClient client = new CloudStackClient(cloudstackApiUrl, cloudstackApiKey, cloudstackApiSecret);
    
    String jenkinsIP = form.get("jenkins-ip");
    String jenkinsUsername = form.get("jenkins-username");
    String jenkinsPassword = form.get("jenkins-password");
    
    Template template = TemplateAPI.listTemplates(client, TemplateFilter.all, null, "gitlab-jenkins",  null).get(0);
    VirtualMachine vm = VirtualMachineAPI.listVirtualMachines(client, null, null, null, template.id, null).get(0);
    VMModel jenkinsVM = new VMModel(vm.id, "system-jenkins", systemGroup.getId(), jenkinsIP, jenkinsUsername, jenkinsPassword);
    jenkinsVM.put("jenkins", true);
    
    String chefServer = form.get("chef-server-ip");
    String chefWorkstation = form.get("chef-workstation-ip");
    String chefUsername = form.get("chef-username");
    String chefPassword = form.get("chef-password");
    
    template = TemplateAPI.listTemplates(client, TemplateFilter.all, null, "chef-server",  null).get(0);
    vm = VirtualMachineAPI.listVirtualMachines(client, null, null, null, template.id, null).get(0);
    VMModel chefServerVM = new VMModel(vm.id, "chef-server", systemGroup.getId(), chefServer, chefUsername, chefPassword);
    
    template = TemplateAPI.listTemplates(client, TemplateFilter.all, null, "chef-workstation",  null).get(0);
    vm = VirtualMachineAPI.listVirtualMachines(client, null, null, null, template.id, null).get(0);
    VMModel chefWorkstationVM = new VMModel(vm.id, "chef-workstation", systemGroup.getId(), chefWorkstation, chefUsername, chefPassword);
    
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("cloudstack-api-url", cloudstackApiUrl);
    properties.put("cloudstack-api-key", cloudstackApiKey);
    properties.put("cloudstack-api-secret", cloudstackApiSecret);
    
    VMHelper.setSystemProperties(properties);
    VMHelper.createSystemVM(jenkinsVM, chefServerVM, chefWorkstationVM);

    return redirect(controllers.vm.routes.VMController.index());
  }
}
