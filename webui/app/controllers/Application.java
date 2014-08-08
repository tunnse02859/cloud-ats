/**
 * 
 */
package controllers;

import java.util.Collection;

import org.ats.component.usersmgt.UserManagementException;
import org.ats.component.usersmgt.feature.Feature;
import org.ats.component.usersmgt.feature.FeatureDAO;
import org.ats.component.usersmgt.feature.Operation;
import org.ats.component.usersmgt.feature.OperationDAO;
import org.ats.component.usersmgt.group.Group;
import org.ats.component.usersmgt.group.GroupDAO;
import org.ats.component.usersmgt.role.Permission;
import org.ats.component.usersmgt.role.PermissionDAO;
import org.ats.component.usersmgt.role.Role;
import org.ats.component.usersmgt.role.RoleDAO;
import org.ats.component.usersmgt.user.User;
import org.ats.component.usersmgt.user.UserDAO;

import com.mongodb.BasicDBObject;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import setup.AuthenticatedInterceptor;
import setup.WizardInterceptor;
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
  
  public static Result doSignup() throws UserManagementException {
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
      
      Feature organization = FeatureDAO.INSTANCE.find(new BasicDBObject("name", "Organization")).iterator().next();
      company.addFeature(organization);
      
      Role administration = new Role("Administration", company.getId());
      company.addRole(administration);
      
      for (Operation operation : organization.getOperations()) {
        administration.addPermission(new Permission(organization.getId(), operation.getId()));
      }
      administration.addUser(admin);
      admin.addRole(administration);
      
      GroupDAO.INSTANCE.create(company);
      UserDAO.INSTANCE.create(admin);
      RoleDAO.INSTANCE.create(administration);
      
      session().put("email", admin.getEmail());
      session().put("user_id", admin.getId());
    } else {
      String email = form.get("email");
      String password = form.get("password");
      User user = new User(null, email);
      user.put("password", password);
      UserDAO.INSTANCE.create(user);
      
      session().put("email", user.getEmail());
      session().put("user_id", user.getId());
    }
    return redirect(controllers.routes.Application.main());
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
    if (user.getString("password").equals(password)) {
      session().put("email", user.getEmail());
      session().put("user_id", user.getId());
      return redirect(controllers.routes.Application.main());
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
  
  @With(AuthenticatedInterceptor.class)
  public static Result main() {
    return ok(main.render());
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
    
    Operation ad = new Operation("Administration");
    organization.addOperation(ad);
    
    FeatureDAO.INSTANCE.create(organization);
    OperationDAO.INSANCE.create(ad);
    
    Group system = new Group("System Admin");
    system.put("system", true);
    system.put("level", 0);
    system.addFeature(organization);
    
    User user = new User(email, email);
    user.put("system", true);
    user.put("password", password);
    user.joinGroup(system);
    UserDAO.INSTANCE.create(user);
    
    system.addUser(user);
    
    Role administration = new Role("Administration", system.getId());
    Permission per = null;
    
    administration.addPermission(per = new Permission(organization.getId(), ad.getId()));
    PermissionDAO.INSTANCE.create(per);
    
    administration.addUser(user);
    
    user.addRole(administration);
    
    system.addRole(administration);
    
    GroupDAO.INSTANCE.create(system);
    RoleDAO.INSTANCE.create(administration);
    
    return redirect(controllers.routes.Application.index());
  }
}
