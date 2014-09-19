/**
 * 
 */
package listener;

import java.util.List;

import models.vm.OfferingModel;
import models.vm.VMModel;

import org.ats.cloudstack.VirtualMachineAPI;
import org.ats.component.usersmgt.Event;
import org.ats.component.usersmgt.EventExecutedException;
import org.ats.component.usersmgt.EventListener;
import org.ats.component.usersmgt.group.Group;

import play.Logger;
import utils.OfferingHelper;
import utils.VMHelper;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 19, 2014
 */
public class DeleteGroupListener implements EventListener {

  @Override
  public void execute(Event event) throws EventExecutedException {
    try {
      if ("delete-group".equals(event.getType())) {
        Group group = new Group(event.getSource());
        List<VMModel> vms = VMHelper.getVMsByGroupID(group.getId());
        for (VMModel vm : vms) {
          VMHelper.removeVM(vm);
          VirtualMachineAPI.destroyVM(VMHelper.getCloudStackClient(), vm.getId(), true);
        }
        
        OfferingModel offering = OfferingHelper.getDefaultOfferingOfGroup(group.getId());
        OfferingHelper.removeOffering(offering);
      }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.error("DeleteGroupListener error", e);
    }
  }
  
}
