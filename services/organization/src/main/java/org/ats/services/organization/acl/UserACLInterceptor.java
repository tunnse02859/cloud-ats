/**
 * 
 */
package org.ats.services.organization.acl;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
public class UserACLInterceptor implements MethodInterceptor {

  public Object invoke(MethodInvocation invocation) throws Throwable {
    return invocation.proceed();
  }
}
