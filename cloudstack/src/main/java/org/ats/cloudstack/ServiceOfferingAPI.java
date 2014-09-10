/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.util.List;

import org.ats.cloudstack.model.ServiceOffering;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class ServiceOfferingAPI extends CloudStackAPI {

  @Deprecated
  public static List<ServiceOffering> listServiceOfferings(String id, String name) throws IOException {
    return listServiceOfferings(CloudStackClient.getInstance(), id, name);
  }
  
  public static List<ServiceOffering> listServiceOfferings(CloudStackClient client, String id, String name) throws IOException {
    StringBuilder sb = new StringBuilder("command=listServiceOfferings&response=json");
    
    if (id != null && !id.isEmpty())
      sb.append("&id=").append(id);
    
    if (name != null && !name.isEmpty())
      sb.append("&name=").append(name);
    String response = request(client, sb.toString());
    return buildModels(ServiceOffering.class, response, "listserviceofferingsresponse", "serviceoffering");
  }
}
