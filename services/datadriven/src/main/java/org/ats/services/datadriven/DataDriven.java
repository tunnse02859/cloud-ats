/**
 * 
 */
package org.ats.services.datadriven;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.ats.common.StringUtil;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.entity.AbstractEntity;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.ats.services.organization.entity.reference.UserReference;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.mongodb.BasicDBObject;

/**
 * @author NamBV2
 *
 * Apr 24, 2015
 */

@SuppressWarnings("serial")
public class DataDriven extends AbstractEntity<DataDriven> {
  
  private ReferenceFactory<TenantReference> tenantRefFactory;
  private ReferenceFactory<UserReference> userRefFactory;
  private ReferenceFactory<SpaceReference> spaceRefFactory;
  

  @Inject
  DataDriven(
      ReferenceFactory<TenantReference> tenantRefFactory, 
      ReferenceFactory<UserReference> userRefFactory,
      ReferenceFactory<SpaceReference> spaceRefFactory,
      OrganizationContext context,
      @Assisted("name") String name, @Assisted("dataSource") String dataSource) {
    
    if (context == null || context.getUser() == null) 
      throw new IllegalStateException("You need logged in system to creat new data driven");
    
    User user = context.getUser();
    this.put("creator", new BasicDBObject("_id", user.getEmail()));
    
    if (context.getSpace() != null) {
      this.put("space", new BasicDBObject("_id", context.getSpace().getId()));
    }
    
    this.put("tenant", user.getTanent().toJSon());
    
    this.put("_id", UUID.randomUUID().toString());
    this.put("name", name);
    this.put("data_source", dataSource);
    this.put("created_date", new Date());
    setActive(true);
    
    this.tenantRefFactory = tenantRefFactory;
    this.userRefFactory = userRefFactory;
    this.spaceRefFactory = spaceRefFactory;
  }
  
  public String getId() {
    return this.getString("_id");
  }
  
  public String getName() {
    return this.getString("name");
  }
  
  public String getDataSource() {
    return this.getString("data_source");
  }
  
  public void setDataSource(String dataset) {
    this.put("data_source", dataset);
  }
  
  public UserReference getCreator() {
    return userRefFactory.create(((BasicDBObject)this.get("creator")).getString("_id"));
  }
  
  public SpaceReference getSpace() {
    if (this.get("space") == null) return null;
    BasicDBObject obj = (BasicDBObject) this.get("space");
    return spaceRefFactory.create(obj.getString("_id"));
  }
  
  public TenantReference getTenant() {
    return tenantRefFactory.create(((BasicDBObject)this.get("tenant")).getString("_id"));
  }
  
  public String transform(String caseId) throws IOException {
    String caseIdHash = caseId.substring(0, 8);
    StringBuilder sb = new StringBuilder();

    sb.append("@DataProvider(name = \"").append(StringUtil.normalizeName(getName())).append(caseIdHash).append("\")\n");
    sb.append("  public static Object[][] ").append(StringUtil.normalizeName(getName())).append(caseIdHash).append("() throws Exception {\n");
    sb.append("    ObjectMapper obj = new ObjectMapper();\n");

    String data = getDataSource().replace("\n", "").replace("\r", "").replace("\\", "\\\\").replace("\"", "\\\"");
    
    sb.append("    JsonNode rootNode = obj.readTree(\"").append(data).append("\");\n\n");
    sb.append("    JsonNode[][] objData = new JsonNode[rootNode.size()][];\n");
    sb.append("    for(int i=0; i<rootNode.size(); i++) {\n");
    sb.append("      objData[i] = new JsonNode[]{ rootNode.get(i) };\n");
    sb.append("    }\n");
    sb.append("    return objData;\n}");
    
    return sb.toString();
  }
}
