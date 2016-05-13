/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.RoleService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Role.Permission;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.SpaceFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.TenantReference;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author TrinhTV3
 *
 */
@CorsComposition.Cors
@Authenticated
public class TenantAdminController extends Controller {
  
  @Inject OrganizationContext context;
  
  @Inject UserService userService;
    
  @Inject SpaceService spaceService;
  
  @Inject SpaceFactory spaceFactory;
  
  @Inject ReferenceFactory<TenantReference> tenantRefFactory;
  
  @Inject RoleService roleService;
  
  public Result list(){
    
    User user = context.getUser();
    user = userService.get(user.getEmail(), "isTenantAdmin");
    if (user.getBoolean("isTenantAdmin")) {
      
      ObjectNode object = Json.newObject();
      BasicDBList listSpace = new BasicDBList();
      PageList<Space> spaces = spaceService.query(new BasicDBObject("tenant", new BasicDBObject("_id", context.getTenant().getId())));
      while (spaces.hasNext()) {
        for (Space space : spaces.next()) {
          listSpace.add(space);
        }
      }
      BasicDBList listUsers = new BasicDBList();
      PageList<User> users = userService.query(new BasicDBObject("isTenantAdmin", true).append("tenant", new BasicDBObject("_id", context.getTenant().getId())));
      while (users.hasNext()) {
        for (User u : users.next()) {
          StringBuilder builder = new StringBuilder();
          for (RoleReference ref : u.getRoles()) {
            Role role = roleService.get(ref.getId());
            for (Permission per : role.getPermissions()) {
              builder.append(per.getRule());
            }
          }
          u.put("permission", builder.toString());
          listUsers.add(u);
        }
      }
      
      object.put("spaces", Json.parse(listSpace.toString()));
      object.put("users", Json.parse(listUsers.toString()));
      
      return ok(object);
    }
    return ok();
  }
  
  public Result search(String text) {
    PageList<User> users = userService.search(text);
    
    ArrayNode array = Json.newObject().arrayNode();
    while (users.hasNext()) {
      for (User user : users.next()) {
        array.add(Json.parse(user.toString()));
      }
    }
    
    return ok(array);
  }
  
  public Result delete() {
    
    String id = request().body().asText();
    spaceService.delete(id);
    return ok();
  }
  
  public Result create(String name) {
    
    Space space = spaceFactory.create(name);
    space.setTenant(tenantRefFactory.create(context.getTenant().getId()));
    spaceService.create(space);
    
    return ok(Json.parse(space.toString()));
  }
  
  public Result addAdmin() {
    JsonNode json = request().body().asJson();
    return ok(json);
  }
}
