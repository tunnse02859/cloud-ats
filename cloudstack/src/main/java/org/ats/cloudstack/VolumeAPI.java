/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.ats.cloudstack.model.Volume;
import org.json.JSONObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 26, 2014
 */
public class VolumeAPI extends CloudStackAPI {

  public static List<Volume> listVolumes(String id, String name, String type, String vmId, ModelFilter<Volume> filter) throws IOException {
    StringBuilder sb = new StringBuilder("command=listVolumes&response=json");
    
    if (id != null && !id.isEmpty())
      sb.append("&id=").append(id);
    
    if (name != null && !name.isEmpty())
      sb.append("&name=").append(name);
    
    if (type != null && !type.isEmpty())
      sb.append("&type=").append(type);
    
    if (vmId != null && !vmId.isEmpty())
      sb.append("&virtualmachineid=").append(vmId);
    
    String response = request(sb.toString());

    return buildModels(Volume.class, response, "listvolumesresponse", "volume", filter);
  }
  
  public static List<Volume> listVolumesNotAttached(String id, String name, String type, String vmId) throws IOException {
    List<Volume> volumes = listVolumes(id, name, type, vmId, new ModelFilter<Volume>() {

      public void doFilter(Collection<Volume> holder, Volume model) {
        if (model.vmId == null) holder.add(model);
      }
    });
    return volumes;
  }
  
  public static boolean clearNotAttachedVolumes() throws IOException {
    boolean success = true;
    for (Volume vol : listVolumesNotAttached(null, null, "DATADISK", null)) {
      success =  success && deleteVolume(vol.id);
    }
    return success;
  }
  
  public static boolean deleteVolume(String id) throws IOException {
    StringBuilder sb = new StringBuilder("command=deleteVolume&response=json&id=").append(id);
    String response = request(sb.toString());
    JSONObject json = new JSONObject(response).getJSONObject("deletevolumeresponse");
    return json.getBoolean("success");
  }
}
