/**
 * 
 */
package controllers;

import java.util.List;

import org.ats.services.OrganizationContext;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.SpaceReference;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import actions.CorsComposition;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 18, 2015
 */
@CorsComposition.Cors
@Authenticated
public class UserController extends Controller {

  @Inject
  OrganizationContext context;

  @Inject
  SpaceService spaceService;

  @Inject
  ReferenceFactory<SpaceReference> spaceRefFactory;

  public Result spaces() {
    User user = context.getUser();
    List<SpaceReference> refs = user.getSpaces();

    ArrayNode spaces = Json.newObject().arrayNode();

    for (SpaceReference ref : refs) {
      spaces.add(Json.parse(ref.get().toString()));
    }
    return ok(spaces);
  }

  public Result goTo(String spaceId) {
    if (spaceId == null || "null".equals(spaceId)) {
      this.context.setSpace(null);
      ObjectNode json = Json.newObject();
      json.put("user", Json.parse(this.context.getUser().toString()));
      json.put("tenant", Json.parse(this.context.getTenant().toString()));
      return ok(json);
    }
    
    OrganizationContext context = spaceService.goTo(spaceRefFactory.create(spaceId));
    ObjectNode json = Json.newObject();
    json.put("user", Json.parse(context.getUser().toString()));
    json.put("tenant", Json.parse(context.getTenant().toString()));
    json.put("space", Json.parse(context.getSpace().toString()));
    
    return ok(json);
  }
}
