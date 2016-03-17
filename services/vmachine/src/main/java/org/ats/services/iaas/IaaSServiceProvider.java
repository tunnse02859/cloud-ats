/**
 * 
 */
package org.ats.services.iaas;

import javax.inject.Provider;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 16, 2015
 */
public class IaaSServiceProvider implements Provider<IaaSService> {
  
  private final OpenStackService opsService;
  
  private final AWSService awsService;
  
  private final StandaloneService standaloneService;
  
  private final Class<IaaSService> iaasClazz;
  
  @SuppressWarnings("unchecked")
  @Inject
  IaaSServiceProvider(StandaloneService standaloneService, OpenStackService opsService, AWSService awsService, @Named("org.ats.cloud.iaas") String iaasClazz) throws ClassNotFoundException {
    this.opsService = opsService;
    this.awsService = awsService;
    this.standaloneService = standaloneService;
    this.iaasClazz = (Class<IaaSService>) Class.forName(iaasClazz);
  }

  @Override
  public IaaSService get() {
    if (iaasClazz.isInstance(standaloneService)) return standaloneService;
    else if (iaasClazz.isInstance(opsService)) return opsService;
    else if (iaasClazz.isInstance(awsService)) return awsService;
    return null;
  }

}
