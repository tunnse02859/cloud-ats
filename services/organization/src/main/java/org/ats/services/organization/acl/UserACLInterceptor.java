/**
 * 
 */
package org.ats.services.organization.acl;

import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.ats.services.OrganizationContext;
import org.ats.services.data.common.Reference;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Feature.Action;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Role.Permission;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.FeatureReference;
import org.ats.services.organization.entity.reference.RoleReference;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class UserACLInterceptor implements MethodInterceptor {
  
  private Provider<OrganizationContext> context;
  
  private Provider<ReferenceFactory<FeatureReference>> featureRefFactory;
  
  @Inject
  public UserACLInterceptor(Provider<OrganizationContext> context, Provider<ReferenceFactory<FeatureReference>> featureRefFactory) {
    this.context = context;
    this.featureRefFactory = featureRefFactory;
  }
  
  public Object invoke(MethodInvocation invocation) throws Throwable {
    if (context.get().getUser() == null) throw new UnAuthenticatedException("You need logging to perform this action");

    Authorized rule = invocation.getMethod().getAnnotation(Authorized.class);
    if (rule == null) return invocation.proceed();;
    
    UnAuthorizationException e = new UnAuthorizationException("The user " + context.get().getUser().getEmail() + " does not have permission to perform " + invocation.getMethod());
    
    String tenant = rule.tenant();
    if (!"*".equals(tenant) && !tenant.equals(context.get().getTenant().getName())) 
      throw e;

    String space = rule.space();
    if (!"*".equals(space) && !space.equals(context.get().getSpace().getName()))
      throw e;
    
    String feature = rule.feature();
    if ("*".equals(feature)) {
      return invocation.proceed();
    }

    User user = context.get().getUser();
    
    boolean hasFeature = false;
    List<Permission> cache = new ArrayList<Permission>();
    
    for (RoleReference roleRef : user.getRoles()) {
      
      Role role = roleRef.get();

      for (Permission perm : role.getPermissions()) {
        Reference<?> featureRef = perm.getFeature();
        if(context.get().getTenant().getFeatures().contains(featureRefFactory.get().create(feature))) {
          if (featureRef == Feature.ANY) {
            return invocation.proceed();
          } else if (feature.equals(featureRef.getId())) {
            hasFeature = true;
          }
        }
      }
      
      if (hasFeature) cache.addAll(role.getPermissions());
    }
    
    if (!hasFeature) throw e;
    
    String action = rule.action();
    if ("*".equals(action)) return invocation.proceed();
    
    for (Permission perm : cache) {
      Action act = perm.getAction();
      if (act == Action.ANY) return invocation.proceed();
      else if (action.equals(act.getName())) return invocation.proceed();
    }
    
    return e;
  }
}
