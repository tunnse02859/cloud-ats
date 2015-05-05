/**
 * 
 */
package org.ats.services;

import org.ats.services.functional.ActionFactory;
import org.ats.services.functional.CaseFactory;
import org.ats.services.functional.FunctionalProjectFactory;
import org.ats.services.functional.FunctionalProjectService;
import org.ats.services.functional.SuiteReference;
import org.ats.services.functional.SuiteService;
import org.ats.services.functional.VariableFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public class FunctionalServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(FunctionalProjectService.class);
    bind(SuiteService.class);
    bind(ActionFactory.class);
    bind(VariableFactory.class);

    install(new FactoryModuleBuilder().build(FunctionalProjectFactory.class));
    install(new FactoryModuleBuilder().build(CaseFactory.class));
    install(new FactoryModuleBuilder().build(new TypeLiteral<ReferenceFactory<SuiteReference>>(){}));
  }

}
