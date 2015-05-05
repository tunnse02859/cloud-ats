package controllers;

import org.ats.services.OrganizationContext;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;

import play.mvc.Controller;
import play.mvc.Result;

import com.google.inject.Inject;

public class Application extends Controller {
  
  @Inject
  private TenantService tenantService;
  
  @Inject
  private TenantFactory tenantFactory;
  
  @Inject
  private ReferenceFactory<TenantReference> tenantRefFactory;
  
  @Inject
  private SpaceService spaceService;
  
  @Inject
  private SpaceFactory spaceFactory;
  
  @Inject
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  
  @Inject
  private UserService userService;
  
  @Inject
  private UserFactory userFactory;
  
  @Inject
  private OrganizationContext context;
  
  public Result index() {
    return ok("Hello REST service");
  }

  public Result createUser() {
    Tenant tenant = tenantFactory.create("Fsoft");
    this.tenantService.create(tenant);
    
    Space space = spaceFactory.create("FSU1.BU11");
    space.setTenant(tenantRefFactory.create(tenant.getId()));
    this.spaceService.create(space);
    
    User user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    user.setTenant(tenantRefFactory.create(tenant.getId()));
    user.joinSpace(spaceRefFactory.create(space.getId()));
    user.setPassword("12345");
    userService.create(user);
    
    user = userFactory.create("tuanhq@cloud-ats.net", "Tuan", "Hoang");
    user.setTenant(tenantRefFactory.create(tenant.getId()));
    user.joinSpace(spaceRefFactory.create(space.getId()));
    user.setPassword("12345");
    userService.create(user);
    
    user = userFactory.create("trinhtv@cloud-ats.net", "Trinh", "Tran");
    user.setTenant(tenantRefFactory.create(tenant.getId()));
    user.joinSpace(spaceRefFactory.create(space.getId()));
    user.setPassword("12345");
    userService.create(user);
    
    user = userFactory.create("nambv@cloud-ats.net", "Nam", "Bui");
    user.setTenant(tenantRefFactory.create(tenant.getId()));
    user.joinSpace(spaceRefFactory.create(space.getId()));
    user.setPassword("12345");
    userService.create(user);
    
    return status(200);
  }
  
  public Result current() {
    StringBuilder sb = new StringBuilder();
    sb.append(context).append("\n");
    sb.append(context.getUser()).append("\n");
    sb.append(context.getTenant()).append("\n");
    sb.append(context.getSpace()).append("\n");
    return ok(sb.toString());
  }

}
