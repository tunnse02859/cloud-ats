/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.util.List;

import org.ats.cloudstack.model.DiskOffering;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class DiskOfferingAPI extends CloudStackAPI {

  public static List<DiskOffering> listDiskOfferings(String id, String name) throws IOException  {
    StringBuilder sb = new StringBuilder("command=listDiskOfferings&response=json");
    
    if (id != null && !id.isEmpty()) 
      sb.append("&id=").append(id);
    
    if (name != null && !name.isEmpty())
      sb.append("&name=").append(name);
    
    String response = request(sb.toString());
    return buildModels(DiskOffering.class, response, "listdiskofferingsresponse", "diskoffering");
  }
}
