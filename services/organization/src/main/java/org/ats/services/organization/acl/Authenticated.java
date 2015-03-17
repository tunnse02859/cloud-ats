/**
 * 
 */
package org.ats.services.organization.acl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 16, 2015
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.TYPE)

public @interface Authenticated {

}
