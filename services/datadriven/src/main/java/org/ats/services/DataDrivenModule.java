/**
 * 
 */
package org.ats.services;

import org.ats.services.datadriven.DataDrivenFactory;
import org.ats.services.datadriven.DataDrivenReference;
import org.ats.services.datadriven.DataDrivenService;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 6, 2015
 */
public class DataDrivenModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DataDrivenService.class);
    
    install(new FactoryModuleBuilder().build(DataDrivenFactory.class));
    install(new FactoryModuleBuilder().build(new TypeLiteral<ReferenceFactory<DataDrivenReference>>(){}));
  }

}
