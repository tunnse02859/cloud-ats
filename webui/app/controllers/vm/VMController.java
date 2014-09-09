/**
 * 
 */
package controllers.vm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import models.vm.VirtualMachine;

import org.ats.component.usersmgt.DataFactory;
import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import controllers.organization.Organization;
import interceptor.AuthenticationInterceptor;
import interceptor.Authorization;
import interceptor.WithSystem;
import interceptor.WizardInterceptor;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
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
    DB vmDB = DataFactory.getDatabase("cloud-ats-vm");
    User currentUser = UserDAO.INSTANCE.findOne(session("user_id"));
    
    if (vmDB.getCollection("cloud-system").count() == 0) {
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
    
    DBCursor cursor = vmDB.getCollection("cloud-system").find(new BasicDBObject("group_id", group.getId()));
    List<VirtualMachine> vms = new ArrayList<VirtualMachine>();
    while (cursor.hasNext()) {
      vms.add(new VirtualMachine().from(cursor.next()));
    }
    
    return ok(index.render(body.render(system, group, vms), "Virtual Machine"));
  }

  @WithSystem
  public static Result doWizard() throws UserManagementException {
    DynamicForm form = Form.form().bindFromRequest();
    Group systemGroup = GroupDAO.INSTANCE.find(new BasicDBObject("system", true)).iterator().next();
    
    String jenkinsIP = form.get("jenkins-ip");
    String jenkinsUsername = form.get("jenkins-username");
    String jenkinsPassword = form.get("jenkins-password");
    
    VirtualMachine jenkinsVM = new VirtualMachine("system-jenkins", systemGroup.getId(), jenkinsIP, jenkinsUsername, jenkinsPassword);
    
    String chefServer = form.get("chef-server-ip");
    String chefWorkstation = form.get("chef-workstation-ip");
    String chefUsername = form.get("chef-username");
    String chefPassword = form.get("chef-password");
    
    VirtualMachine chefServerVM = new VirtualMachine("chef-server", systemGroup.getId(), chefServer, chefUsername, chefPassword);
    VirtualMachine chefWorkstationVM = new VirtualMachine("chef-workstation", systemGroup.getId(), chefWorkstation, chefUsername, chefPassword);
    
     String cloudstackApiUrl = form.get("cloudstack-api-url");
     String cloudstackApiKey = form.get("cloudstack-api-key");
     String cloudstackApiSecret = form.get("cloudstack-api-secret");
     
     DB vmDB = DataFactory.getDatabase("cloud-ats-vm");
     DBCollection col = vmDB.getCollection("cloud-system");
     
     col.insert(BasicDBObjectBuilder.start("_id", "cloudstack-api-url").append("value", cloudstackApiUrl).get());
     col.insert(BasicDBObjectBuilder.start("_id", "cloudstack-api-key").append("value", cloudstackApiKey).get());
     col.insert(BasicDBObjectBuilder.start("_id", "cloudstack-api-secret").append("value", cloudstackApiSecret).get());
     
     col.insert(jenkinsVM, chefServerVM, chefWorkstationVM);
     
    return redirect(controllers.vm.routes.VMController.index());
  }
}
