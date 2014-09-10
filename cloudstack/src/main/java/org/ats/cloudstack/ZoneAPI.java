/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.util.List;

import org.ats.cloudstack.model.Zone;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class ZoneAPI extends CloudStackAPI {

  @Deprecated
  public static List<Zone> listAvailableZones() throws IOException {
    return listAvailableZones(CloudStackClient.getInstance());
  }
  
  public static List<Zone> listAvailableZones(CloudStackClient client) throws IOException {
    String cmd = "command=listZones&available=true&response=json";
    String response = request(client, cmd);
    return buildModels(Zone.class, response, "listzonesresponse", "zone");
  }
}
