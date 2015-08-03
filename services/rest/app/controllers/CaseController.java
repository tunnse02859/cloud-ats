/**
 * 
 */
package controllers;

import org.ats.common.PageList;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseService;
import org.ats.services.organization.acl.Authenticated;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.inject.Inject;

import actions.CorsComposition;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Aug 1, 2015
 */
@CorsComposition.Cors
@Authenticated
public class CaseController extends Controller {

  @Inject CaseService caseService;
  
  public Result list(String projectId) {
    
    PageList<Case> list = caseService.getCases(projectId);
    ArrayNode array = Json.newObject().arrayNode();
    while(list.hasNext()) {
      for (Case caze : list.next()) {
        array.add(Json.parse(caze.toString()));
      }
    }
    return ok(array);
  }
}
