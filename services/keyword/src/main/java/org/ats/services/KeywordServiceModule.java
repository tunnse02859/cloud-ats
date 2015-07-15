/**
 * 
 */
package org.ats.services;

import org.ats.services.keyword.ActionFactory;
import org.ats.services.keyword.CaseFactory;
import org.ats.services.keyword.CaseReference;
import org.ats.services.keyword.CaseService;
import org.ats.services.keyword.KeywordProjectFactory;
import org.ats.services.keyword.KeywordProjectService;
import org.ats.services.keyword.SuiteReference;
import org.ats.services.keyword.SuiteService;
import org.ats.services.keyword.VariableFactory;
import org.ats.services.organization.entity.fatory.ReferenceFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 8, 2015
 */
public class KeywordServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(KeywordProjectService.class);
    bind(CaseService.class);
    bind(SuiteService.class);
    bind(ActionFactory.class);
    bind(VariableFactory.class);

    install(new FactoryModuleBuilder().build(KeywordProjectFactory.class));
    install(new FactoryModuleBuilder().build(CaseFactory.class));
    install(new FactoryModuleBuilder().build(new TypeLiteral<ReferenceFactory<CaseReference>>(){}));
    install(new FactoryModuleBuilder().build(new TypeLiteral<ReferenceFactory<SuiteReference>>(){}));
  }
}
