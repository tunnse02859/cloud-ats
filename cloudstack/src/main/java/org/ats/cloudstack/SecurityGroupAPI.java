/**
 * 
 */
package org.ats.cloudstack;

import java.io.IOException;
import java.util.List;

import org.ats.cloudstack.model.SecurityGroup;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 25, 2014
 */
public class SecurityGroupAPI extends CloudStackAPI {

  @Deprecated
  public static List<SecurityGroup> listSecurityGroups(String id, String domainId, String account, String securityGroupName) throws IOException {
    return listSecurityGroups(CloudStackClient.getInstance(), id, domainId, account, securityGroupName);
  }

  public static List<SecurityGroup> listSecurityGroups(CloudStackClient client, String id, String domainId, String account, String securityGroupName) throws IOException {
    StringBuilder sb = new StringBuilder("command=listSecurityGroups&response=json");

    if (id != null && !id.isEmpty())
      sb.append("&id=").append(id);

    if (domainId != null && !domainId.isEmpty())
      sb.append("&domainid=").append(domainId);

    if(account != null && !account.isEmpty())
      sb.append("&account=").append(account);

    if (securityGroupName != null && !securityGroupName.isEmpty())
      sb.append("&securitygroupname=").append(securityGroupName);

    String response = request(client, sb.toString());
    return buildModels(SecurityGroup.class, response, "listsecuritygroupsresponse", "securitygroup");
  }
}
