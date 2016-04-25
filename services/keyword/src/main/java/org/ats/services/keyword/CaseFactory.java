/**
 * 
 */
package org.ats.services.keyword;

import org.ats.services.datadriven.DataDrivenReference;

import com.google.inject.assistedinject.Assisted;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 4, 2015
 */
public interface CaseFactory {

  public Case create(@Assisted("projectId") String projectId, 
      @Assisted("name") String name,
      @Assisted("dataDriven") DataDrivenReference ref,
      @Assisted("creator") String creator);
}
