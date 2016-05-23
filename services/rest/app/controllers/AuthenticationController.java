/**
 * 
 */
package controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.TenantService;
import org.ats.services.organization.UserService;
import org.ats.services.organization.base.AuthenticationService;
import org.ats.services.organization.entity.Role;
import org.ats.services.organization.entity.Space;
import org.ats.services.organization.entity.Tenant;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.fatory.UserFactory;
import org.ats.services.organization.entity.reference.RoleReference;
import org.ats.services.organization.entity.reference.TenantReference;
import org.bson.BSONObject;

import play.Logger;
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
import com.mongodb.DBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Mar 20, 2015
 */
@CorsComposition.Cors
public class AuthenticationController extends Controller {
  
  @Inject
  private AuthenticationService<User> authenService;
  
  @Inject
  private OrganizationContext context;
  
  @Inject
  private TenantService tenantService;
  
  @Inject
  private SpaceService spaceService;
  
  @Inject
  private UserFactory userFactory;
  
  @Inject
  private ReferenceFactory<TenantReference> tenantRef;
  
  @Inject
  private UserService userService;
  
  public Result checkAccount() {
    
    String email = request().getQueryString("email");
    
    Logger.info("Check account in controller");
    if (userService.get(email) != null) {
      return ok("false");
    }
    return ok();
  }
  public Result login() {
    JsonNode json = request().body().asJson();

    String email = json.get("email").asText();
    String password = json.get("password").asText();
    
    String token = authenService.logIn(email, password);
    if (token == null) return unauthorized();
    
    ObjectNode authTokenJson = Json.newObject();
    authTokenJson.put(AuthenticationService.AUTH_TOKEN, token);
    return ok(authTokenJson);
  }
  
  public Result logout() {
    authenService.logOut();
    return ok();
  }
  
  public Result register() {
    
    JsonNode json = request().body().asJson();
    
    String email = json.get("email").asText();
    if (userService.get(email) != null) {
      return status(401);
    }
    String password = json.get("password").asText();
    String tenantId = json.get("tenant").asText();
    
    String firstName = json.get("firstname").asText();
    String lastName = json.get("lastname").asText();
    User user = userFactory.create(email, firstName, lastName);
    
    /*if (json.get("space").asText() != null && !("".equals(json.get("space").asText()))) {
      spaceName = json.get("space").asText();
      
      Space space = spaceFactory.create(spaceName);
      
      space.setTenant(tenantRef.create(tenantId));
      
      spaceService.create(space);
      user.joinSpace(spaceRef.create(space.getId()));
    }*/
    
    user.setTenant(tenantRef.create(tenantId));
    user.setPassword(password);
    
    userService.create(user);
    
    String token = authenService.logIn(email, password);
    ObjectNode authTokenJson = Json.newObject();
    authTokenJson.put(AuthenticationService.AUTH_TOKEN, token);
    
    return ok(authTokenJson);
  }
  
  public Result tenants() {
    
    PageList<Tenant> list = tenantService.list();
    
    ArrayNode array = Json.newObject().arrayNode();
    
    while (list.hasNext()) {
      List<Tenant> listUser = list.next();
      for (Tenant tenant : listUser) {
        ObjectNode jsonTenant = Json.newObject();
        jsonTenant.put("_id", tenant.getId());
        array.add(jsonTenant);
      }
    }
    
    return ok(array);
  }
  
//  public Result spaces(String tenantId){
//	  PageList<Space> listSpace = spaceService.query(new BasicDBObject("tenant", new BasicDBObject("_id", tenantId)));
//	  ArrayNode jsonSpace = Json.newObject().arrayNode();
//	  while(listSpace.hasNext()) {
//      	List<Space> spaces = listSpace.next();
//      	for (Space space : spaces) {
//              jsonSpace.add(Json.parse(space.toString()));
// 		}
//      }
//	  return ok(jsonSpace);
//	  
//  }
  
  public Result current() {
    ObjectNode json = Json.newObject();
    User user = null;
    if (context.getUser() != null) {
      user = userService.get(context.getUser().getEmail());
      if (user != null) {
        BasicDBList perms = new BasicDBList();
        for (RoleReference roleRef : user.getRoles()) {
          Role role = roleRef.get();
          for (Role.Permission perm : role.getPermissions()) {
            perms.add(perm.getRule());
          }
        }
        user.put("perms", perms);
        json.put("user", Json.parse(user.toString()));
      }
    }
    if (context.getTenant() != null) 
      json.put("tenant", Json.parse(context.getTenant().toString()));
    
    if (context.getSpace() != null)
      json.put("space", Json.parse(context.getSpace().toString()));
    return ok(json);
  }
}
