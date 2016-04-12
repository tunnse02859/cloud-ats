/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services;

import org.ats.services.project.MixProjectFactory;
import org.ats.services.project.MixProjectService;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author TrinhTV3
 *
 */
public class MixProjectModule extends AbstractModule {

  @Override
  protected void configure() {
    
    bind(MixProjectService.class);
    install(new FactoryModuleBuilder().build(MixProjectFactory.class));
  }

}
