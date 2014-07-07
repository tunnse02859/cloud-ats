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

  public static List<Zone> listAvailableZones() throws IOException {
    String cmd = "command=listZones&available=true&response=json";
    String response = request(cmd);
    return buildModels(Zone.class, response, "listzonesresponse", "zone");
  }
}
