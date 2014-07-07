/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.util.List;

import org.apache.cloudstack.api.ApiConstants;
import org.ats.cloudstack.model.DiskOffering;
import org.ats.cloudstack.model.VirtualMachine;
import org.json.JSONObject;

import com.cloud.template.VirtualMachineTemplate.TemplateFilter;
import com.cloud.vm.VirtualMachine.State;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 24, 2014
 */
public class VirtualMachineAPI extends CloudStackAPI {
  
  public static String[] quickDeployVirtualMachine(String name, String template, String service, String disk) throws IOException {
    String zoneId = ZoneAPI.listAvailableZones().get(0).id;
    String templateId = TemplateAPI.listTemplates(TemplateFilter.all, null, template, zoneId).get(0).id;
    String serviceOfferingId = ServiceOfferingAPI.listServiceOfferings(null, service).get(0).id;
    
    StringBuilder sb = new StringBuilder("command=deployVirtualMachine&response=json");
    sb.append("&zoneid=").append(zoneId);
    sb.append("&templateid=").append(templateId);
    sb.append("&serviceofferingid=").append(serviceOfferingId);
    
    if (disk != null && !disk.isEmpty()) {
      DiskOffering dof = DiskOfferingAPI.listDiskOfferings(null, disk).get(0);
      sb.append("&diskofferingid=").append(dof.id);
      sb.append("&size=").append(dof.diskSize);
    }
    
    if (name != null && !name.isEmpty()) {
      sb.append("&name=").append(name);
      sb.append("&displayname=").append(name);
    }
    
    String response = request(sb.toString());
    JSONObject json = new JSONObject(response).getJSONObject("deployvirtualmachineresponse");
    String vmId = json.getString("id");
    String jobId = json.getString("jobid");
    return new String[] { vmId, jobId };
  }
  
  public static String  destroyVM(String id, boolean expunge) throws IOException {
    StringBuilder sb = new StringBuilder("command=destroyVirtualMachine&response=json");
    sb.append("&id=").append(id);
    sb.append("&expunge=").append(expunge);
    String response = request(sb.toString());
    JSONObject json = new JSONObject(response).getJSONObject("destroyvirtualmachineresponse");
    return json.getString("jobid");
  }

  public static List<VirtualMachine> listVirtualMachines(String id, String name, State state, String templateId, ApiConstants.VMDetails details) throws IOException {
    StringBuilder sb = new StringBuilder("command=listVirtualMachines&response=json");
    
    if (id != null && !id.isEmpty())
      sb.append("&id=").append(id);
    
    if (name != null && !name.isEmpty()) 
      sb.append("&name=").append(name);
    
    if (state != null)
      sb.append("&state=").append(state);
    
    if(templateId != null && !templateId.isEmpty())
      sb.append("&templateid=").append(templateId);
    
    if (details != null)
      sb.append("&details=").append(details);
    
    String command = sb.toString();
    String response = request(command);
    return buildModels(VirtualMachine.class, response, "listvirtualmachinesresponse", "virtualmachine");
  }
  
  public static VirtualMachine findVMById(String vmId, ApiConstants.VMDetails details) throws IOException {
    List<VirtualMachine> list = listVirtualMachines(vmId, null, null, null, details);
    return list.get(0);
  }
}
