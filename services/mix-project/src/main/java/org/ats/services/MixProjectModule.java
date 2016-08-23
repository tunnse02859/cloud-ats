/**
 * TrinhTV3@fsoft.com.vn
 */
package org.ats.services;

import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.project.MixProjectFactory;
import org.ats.services.project.MixProjectService;
import org.ats.services.schedule.ScheduleFactory;
import org.ats.services.schedule.ScheduleReference;
import org.ats.services.schedule.ScheduleService;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author TrinhTV3
 *
 */
public class MixProjectModule extends AbstractModule {

  @Override
  protected void configure() {
    
    bind(MixProjectService.class);
    bind(ScheduleService.class);
    install(new FactoryModuleBuilder().build(MixProjectFactory.class));
    install(new FactoryModuleBuilder().build(ScheduleFactory.class));
    install(new FactoryModuleBuilder().build(new TypeLiteral<ReferenceFactory<ScheduleReference>>(){}));
  }

}
