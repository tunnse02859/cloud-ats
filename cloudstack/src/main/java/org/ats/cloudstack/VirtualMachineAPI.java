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
  
  @Deprecated
  public static String[] quickDeployVirtualMachine(String name, String template, String service, String disk) throws IOException {
    return quickDeployVirtualMachine(CloudStackClient.getInstance(), name, template, service, disk);
  }
  
  public static String[] quickDeployVirtualMachine(CloudStackClient client, String name, String template, String service, String disk) throws IOException {
    String zoneId = ZoneAPI.listAvailableZones(client).get(0).id;
    String templateId = TemplateAPI.listTemplates(client, TemplateFilter.all, null, template, zoneId).get(0).id;
    String serviceOfferingId = ServiceOfferingAPI.listServiceOfferings(client, null, service).get(0).id;
    
    StringBuilder sb = new StringBuilder("command=deployVirtualMachine&response=json");
    sb.append("&zoneid=").append(zoneId);
    sb.append("&templateid=").append(templateId);
    sb.append("&serviceofferingid=").append(serviceOfferingId);
    
    if (disk != null && !disk.isEmpty()) {
      DiskOffering dof = DiskOfferingAPI.listDiskOfferings(client, null, disk).get(0);
      sb.append("&diskofferingid=").append(dof.id);
      sb.append("&size=").append(dof.diskSize);
    }
    
    if (name != null && !name.isEmpty()) {
      sb.append("&name=").append(name);
      sb.append("&displayname=").append(name);
    }
    
    String response = request(client, sb.toString());
    JSONObject json = new JSONObject(response).getJSONObject("deployvirtualmachineresponse");
    String vmId = json.getString("id");
    String jobId = json.getString("jobid");
    return new String[] { vmId, jobId };
  }
  
  @Deprecated
  public static String  destroyVM(String id, boolean expunge) throws IOException {
    return destroyVM(CloudStackClient.getInstance(), id, expunge);
  }
  
  public static String  destroyVM(CloudStackClient client, String id, boolean expunge) throws IOException {
    StringBuilder sb = new StringBuilder("command=destroyVirtualMachine&response=json");
    sb.append("&id=").append(id);
    sb.append("&expunge=").append(expunge);
    String response = request(client, sb.toString());
    JSONObject json = new JSONObject(response).getJSONObject("destroyvirtualmachineresponse");
    return json.getString("jobid");
  }
  
  public static String startVM(CloudStackClient client, String id) throws IOException {
    StringBuilder sb = new StringBuilder("command=startVirtualMachine&response=json");
    sb.append("&id=").append(id);
    String response = request(client, sb.toString());
    JSONObject json = new JSONObject(response).getJSONObject("startvirtualmachineresponse");
    return json.getString("jobid");
  }
  
  public static String stopVM(CloudStackClient client, String id, boolean forced) throws IOException {
    StringBuilder sb = new StringBuilder("command=stopVirtualMachine&response=json");
    sb.append("&id=").append(id).append("&forced=").append(forced);
    String response = request(client, sb.toString());
    JSONObject json = new JSONObject(response).getJSONObject("stopvirtualmachineresponse");
    return json.getString("jobid");
  }
  
  public static String restoreVM(CloudStackClient client, String id, String templateId) throws IOException {
    StringBuilder sb = new StringBuilder("command=restoreVirtualMachine&response=json");
    sb.append("&virtualmachineid=").append(id);
    if (templateId != null && !templateId.isEmpty()) {
      sb.append("&templateid=").append(templateId);
    }
    String response = request(client, sb.toString());
    JSONObject json = new JSONObject(response).getJSONObject("restorevmresponse");
    return json.getString("jobid");
  }

  @Deprecated
  public static List<VirtualMachine> listVirtualMachines(String id, String name, State state, String templateId, ApiConstants.VMDetails details) throws IOException {
    return listVirtualMachines(CloudStackClient.getInstance(), id, name, state, templateId, details);
  }
  
  public static List<VirtualMachine> listVirtualMachines(CloudStackClient client, String id, String name, State state, String templateId, ApiConstants.VMDetails details) throws IOException {
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
    String response = request(client, command);
    return buildModels(VirtualMachine.class, response, "listvirtualmachinesresponse", "virtualmachine");
  }
  
  @Deprecated
  public static VirtualMachine findVMById(String vmId, ApiConstants.VMDetails details) throws IOException {
    return findVMById(CloudStackClient.getInstance(), vmId, details);
  }
  
  public static VirtualMachine findVMById(CloudStackClient client, String vmId, ApiConstants.VMDetails details) throws IOException {
    List<VirtualMachine> list = listVirtualMachines(client, vmId, null, null, null, details);
    return list.isEmpty() ? null : list.get(0);
  }
}
