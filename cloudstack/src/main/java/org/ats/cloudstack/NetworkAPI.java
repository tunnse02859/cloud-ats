/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.util.List;

import org.ats.cloudstack.model.Network;

import com.cloud.network.Networks.TrafficType;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class NetworkAPI extends CloudStackAPI {

  @Deprecated
  public static List<Network> listNetworks(String id, String zoneId, TrafficType trafficType) throws IOException {
    return listNetworks(CloudStackClient.getInstance(), id, zoneId, trafficType);
  }
  
  public static List<Network> listNetworks(CloudStackClient client, String id, String zoneId, TrafficType trafficType) throws IOException {
    StringBuilder sb = new StringBuilder("command=listNetworks&response=json");
    
    if (id != null && !id.isEmpty())
      sb.append("&id=").append(id);
    
    if (zoneId != null && !zoneId.isEmpty())
      sb.append("&zoneid=").append(zoneId);
    
    if (trafficType != null)
      sb.append("&traffictype=").append(trafficType);
    
    String response = request(client, sb.toString());
    return buildModels(Network.class, response, "listnetworksresponse", "network");
  }
}
