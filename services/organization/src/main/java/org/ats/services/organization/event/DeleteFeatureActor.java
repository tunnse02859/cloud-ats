package org.ats.services.organization.event;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.event.Event;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.entity.Feature;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.FeatureReference;

import akka.actor.UntypedActor;

import com.google.inject.Inject;

public class DeleteFeatureActor extends UntypedActor {

  @Inject
  private TenantService tenantService;
  
  @Inject
  private ReferenceFactory<FeatureReference> featureRefFactory;
  
  @Inject
  private Logger logger;
  
  @Override
  public void onReceive(Object message) throws Exception {

    if (message instanceof Event) {
      Event event = (Event) message;
      if ("delete-feature".equals(event.getName())) {
        Feature feature = (Feature) event.getSource();
        FeatureReference ref = featureRefFactory.create(feature.getId());
        process(ref);
      }
    } else {
      unhandled(message);
    }
  }
  
  private void process(FeatureReference ref) {
    
    logger.info("Process event delete-feature " + ref.toJSon());
    
    PageList<Tenant> listTenant = tenantService.findIn("features", ref);
    listTenant.setSortable(new MapBuilder<String, Boolean>("created_date", true).build());
    
    List<Tenant> holder = new ArrayList<Tenant>();
    while(listTenant.hasNext()) {
      for (Tenant tenant : listTenant.next()) {
        holder.add(tenant);
      }
    }
    
    for (Tenant tenant : holder) {
      tenant.removeFeature(ref);
      tenantService.update(tenant);
    }
    
    //send processed event to listener
    if (!"deadLetters".equals(getSender().path().name())) {
      getSender().tell(ref, getSelf());
    }
  }

}
