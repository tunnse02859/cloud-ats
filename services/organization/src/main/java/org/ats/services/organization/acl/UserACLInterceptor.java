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
import org.ats.services.organization.entity.reference.RoleReference;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class UserACLInterceptor implements MethodInterceptor {
  
  private Provider<OrganizationContext> provider;
  
  @Inject
  public UserACLInterceptor(Provider<OrganizationContext> provider) {
    this.provider = provider;
  }
  
  public Object invoke(MethodInvocation invocation) throws Throwable {
    OrganizationContext context = provider.get();
    if (context.getUser() == null) throw new UnAuthenticatedException("You need logging to perform this action");
    
    Authorized rule = invocation.getMethod().getAnnotation(Authorized.class);
    
    UnAuthorizationException e = new UnAuthorizationException("The user " + context.getUser().getEmail() + " does not have permission to perform " + invocation.getMethod());
    
    String tenant = rule.tenant();
    if (!"*".equals(tenant) && !tenant.equals(context.getTenant().getName())) 
      throw e;

    String space = rule.space();
    if (!"*".equals(space) && !space.equals(context.getSpace().getName()))
      throw e;
    
    String feature = rule.feature();
    if ("*".equals(feature)) return invocation.proceed();

    User user = context.getUser();
    
    boolean hasFeature = false;
    List<Permission> cache = new ArrayList<Permission>();
    
    for (RoleReference roleRef : user.getRoles()) {
      
      Role role = roleRef.get();

      for (Permission perm : role.getPermissions()) {
        Reference<?> featureRef = perm.getFeature();
        if (featureRef == Feature.ANY) {
          return invocation.proceed();
        } else if (feature.equals(featureRef.getId())) {
          hasFeature = true;
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
