/**
 * 
 */
package org.ats.services.organization.entity.fatory;

import org.ats.services.data.common.Reference;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 24, 2015
 */
public interface ReferenceFactory<R extends Reference<?>> {

  public R create(@Assisted("id") String id);
}
