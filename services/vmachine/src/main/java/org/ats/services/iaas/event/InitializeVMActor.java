/**
 * 
 */
package org.ats.services.iaas.event;

import java.util.logging.Logger;

import org.ats.services.event.Event;
import org.ats.services.event.EventFactory;
import org.ats.services.iaas.IaaSServiceProvider;
import org.ats.services.vmachine.VMachine;
import org.ats.services.vmachine.VMachineService;

import com.google.inject.Inject;

import akka.actor.UntypedActor;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 24, 2015
 */
public class InitializeVMActor extends UntypedActor {
  
@Inject VMachineService vmachineService;
  
  @Inject IaaSServiceProvider iaasProvider;
  
  @Inject EventFactory eventFactory;
  
  @Inject Logger logger;

  @Override
  public void onReceive(Object obj) throws Exception {
    if (obj instanceof Event) {
      
      Event event = (Event) obj;
      if ("initialize-vm".equals(event.getName())) {
        VMachine vm = (VMachine) event.getSource();
        
        if (iaasProvider.get().isVMReady(vm)) {
          if (vm.isSystem()) {
            iaasProvider.get().initSystemVM(vm);
          } else if (vm.hasUI()) {
            iaasProvider.get().initTestVmUI(vm);
          } else {
            iaasProvider.get().initTestVMNonUI(vm);
          }
        } else {
          Thread.sleep(5000);
          event = eventFactory.create(vm, "initialize-vm");
          event.broadcast();
        }
      }
      
    } else {
      unhandled(obj);
    }
  }

}
