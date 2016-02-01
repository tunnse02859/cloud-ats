/**
 * 
 */
package org.ats.services;

import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.performance.JMeterScriptReference;
import org.ats.services.performance.JMeterScriptService;
import org.ats.services.performance.PerformanceProjectFactory;
import org.ats.services.performance.PerformanceProjectService;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Jun 9, 2015
 */
public class PerformanceServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PerformanceProjectService.class);
    bind(JMeterScriptService.class);
    install(new FactoryModuleBuilder().build(PerformanceProjectFactory.class));
    install(new FactoryModuleBuilder().build(new TypeLiteral<ReferenceFactory<JMeterScriptReference>>(){}));
    
  }

}
