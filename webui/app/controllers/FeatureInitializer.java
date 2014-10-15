/**
 * 
 */
package controllers;

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

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 15, 2014
 */
public class FeatureInitializer {
  
  public static void createOrganizationFeature(String adminEmail, String adminPassword) throws UserManagementException {
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
    
    User root = new User(adminEmail, adminEmail);
    root.put("system", true);
    root.put("password", adminPassword);
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
  }

  public static void createVMFeature(User rootUser, Group systemGroup) throws UserManagementException {
    Feature feature = new Feature("Virtual Machine");
    
    Operation o1 = new Operation("Manage System VM");
    Operation o2 = new Operation("Manage Test VM");
    
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
