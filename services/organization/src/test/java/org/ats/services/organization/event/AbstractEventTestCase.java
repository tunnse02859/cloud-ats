/**
 * 
 */
package org.ats.services.organization.event;

import org.ats.services.organization.AbstractTestCase;
import org.ats.services.organization.ActivationService;
import org.ats.services.organization.FeatureService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.FeatureFactory;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.FeatureReference;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;

/**
 * @author TrinhTV3
 *
 * Email: TrinhTV3@fsoft.com.vn
 */
public abstract class AbstractEventTestCase extends AbstractTestCase {

  protected TenantService tenantService;
  protected TenantFactory tenantFactory;
  protected ReferenceFactory<TenantReference> tenantRefFactory;
  
  protected SpaceService spaceService;
  protected SpaceFactory spaceFactory;
  protected ReferenceFactory<SpaceReference> spaceRefFactory;
  
  protected RoleService roleService;
  protected RoleFactory roleFactory;
  protected ReferenceFactory<RoleReference> roleRefFactory;
  protected PermissionFactory permFactory;
  
  protected UserService userService;
  protected UserFactory userFactory;

  protected FeatureFactory featureFactory;
  protected ReferenceFactory<FeatureReference> featureRefFactory;
  protected FeatureService featureService;
  
  protected ActivationService activationService;
  
  protected Role admin;
  protected Role tester;
  
  public void init() throws Exception {
    super.init();
    this.tenantService = injector.getInstance(TenantService.class);
    this.tenantFactory = injector.getInstance(TenantFactory.class);
    this.tenantRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<TenantReference>>(){}));
    
    this.spaceService = injector.getInstance(SpaceService.class);
    this.spaceFactory = injector.getInstance(SpaceFactory.class);
    this.spaceRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<SpaceReference>>(){}));
    
    this.roleService = injector.getInstance(RoleService.class);
    this.roleFactory = injector.getInstance(RoleFactory.class);
    this.roleRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<RoleReference>>(){}));
    this.permFactory = injector.getInstance(PermissionFactory.class);
    
    this.userService = injector.getInstance(UserService.class);
    this.userFactory = injector.getInstance(UserFactory.class);
    
    this.featureFactory = injector.getInstance(FeatureFactory.class);
    this.featureService = injector.getInstance(FeatureService.class);
    this.featureRefFactory = injector.getInstance(Key.get(new TypeLiteral<ReferenceFactory<FeatureReference>>(){}));
    this.activationService = injector.getInstance(ActivationService.class);
    System.out.println("Start initialize data");
    initData();
  }
  
  private void initData() {
    Tenant tenant = tenantFactory.create("Fsoft");
    
    FeatureReference feature1 = featureRefFactory.create("performace");
    FeatureReference feature2 = featureRefFactory.create("functional");
    FeatureReference feature3 = featureRefFactory.create("organization");
    
    featureService.create(featureFactory.create(feature1.getId()), featureFactory.create(feature2.getId()), featureFactory.create(feature3.getId()));
    tenant.addFeature(feature1, feature2, feature3);
    tenantService.create(tenant);
    
    Tenant tenant1 = tenantFactory.create("Fsoft1");
    tenant1.addFeature(feature1, feature2, feature3);
    tenantService.create(tenant1);
    
    Tenant tenant2 = tenantFactory.create("Fsoft2");
    tenant2.addFeature(feature1, feature2, feature3);
    tenantService.create(tenant2);
    
    Space space = spaceFactory.create("FSU1.BU11");
    space.setTenant(tenantRefFactory.create(tenant.getId()));
    
    Space space1 = spaceFactory.create("FSU1.Bu12");
    space1.setTenant(tenantRefFactory.create(tenant.getId()));
    
    admin = roleFactory.create("admin");
    admin.setSpace(spaceRefFactory.create(space.getId()));
    admin.addPermission(permFactory.create("*:*@Fsoft:*"));
    
    tester = roleFactory.create("tester");
    tester.setSpace(spaceRefFactory.create(space.getId()));
    tester.addPermission(permFactory.create("test:*@Fsoft:" + space.getId()));
    
    space.addRole(roleRefFactory.create(admin.getId()), roleRefFactory.create(tester.getId()));
    spaceService.create(space, space1);
    roleService.create(admin, tester);
    
    User user = userFactory.create("haint@cloud-ats.net", "Hai", "Nguyen");
    user.setTenant(tenantRefFactory.create(tenant.getId()));
    user.joinSpace(spaceRefFactory.create(space.getId()));
    user.addRole(roleRefFactory.create(admin.getId()), roleRefFactory.create(tester.getId()));
    userService.create(user);
    
  }
}
