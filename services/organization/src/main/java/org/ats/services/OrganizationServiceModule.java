/**
 * 
 */
package org.ats.services;

import org.ats.services.organization.FeatureService;
import org.ats.services.organization.MongoAuthenticationService;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.acl.UserACLInterceptor;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.FeatureFactory;
import org.ats.services.organization.entity.fatory.FeatureReferenceFactory;
import org.ats.services.organization.entity.fatory.PermissionFactory;
import org.ats.services.organization.entity.fatory.RoleFactory;
import org.ats.services.organization.entity.fatory.RoleReferenceFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.fatory.SpaceReferenceFactory;
import org.ats.services.organization.entity.fatory.TenantFactory;
import org.ats.services.organization.entity.fatory.TenantReferenceFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.fatory.UserReferenceFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 9, 2015
 */
public class OrganizationServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    
    //Bind entity factory
    install(new FactoryModuleBuilder().build(UserFactory.class));
    install(new FactoryModuleBuilder().build(TenantFactory.class));
    install(new FactoryModuleBuilder().build(SpaceFactory.class));
    install(new FactoryModuleBuilder().build(RoleFactory.class));
    install(new FactoryModuleBuilder().build(FeatureFactory.class));
    install(new FactoryModuleBuilder().build(PermissionFactory.class));

    //Bind entity reference factory
    install(new FactoryModuleBuilder().build(UserReferenceFactory.class));
    install(new FactoryModuleBuilder().build(TenantReferenceFactory.class));
    install(new FactoryModuleBuilder().build(SpaceReferenceFactory.class));
    install(new FactoryModuleBuilder().build(RoleReferenceFactory.class));
    install(new FactoryModuleBuilder().build(FeatureReferenceFactory.class));

    //Bind services 
    bind(UserService.class);
    bind(TenantService.class);
    bind(SpaceService.class);
    bind(RoleService.class);
    bind(FeatureService.class);
    
    //bind context
    bind(OrganizationContext.class);
    
    //bind authentication service
    bind(new TypeLiteral<AuthenticationService<User>>(){})
      .to(new TypeLiteral<MongoAuthenticationService>(){});
    
    //interceptor
    bindInterceptor(
        Matchers.annotatedWith(Authenticated.class), 
        Matchers.any(), 
        new UserACLInterceptor(getProvider(OrganizationContext.class)));
  }

}
