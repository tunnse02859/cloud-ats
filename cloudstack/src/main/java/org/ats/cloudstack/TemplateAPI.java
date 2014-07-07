/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.util.List;

import org.ats.cloudstack.model.Template;

import com.cloud.template.VirtualMachineTemplate.TemplateFilter;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class TemplateAPI extends CloudStackAPI {

  public static List<Template> listTemplates(TemplateFilter filter, String id, String name, String zoneId) throws IOException {
    StringBuilder sb = new StringBuilder("command=listTemplates&response=json&templatefilter=").append(filter);
    
    if (id != null && !id.isEmpty())
      sb.append("&id=").append(id);
    
    if (name != null && !name.isEmpty())
      sb.append("&name=").append(name);
    
    if (zoneId != null && !zoneId.isEmpty())
      sb.append("&zoneid=").append(zoneId);
    
    String response = request(sb.toString());
    return buildModels(Template.class, response, "listtemplatesresponse", "template");
  }
}
