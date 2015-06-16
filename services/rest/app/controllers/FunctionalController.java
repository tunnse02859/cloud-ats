/**
 * 
 */
package controllers;

import java.io.File;

import org.ats.services.organization.acl.Authenticated;

import com.fasterxml.jackson.databind.JsonNode;

import actions.CorsComposition;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

/**
 * @author NamBV2
 *
 * Jun 15, 2015
 */

@CorsComposition.Cors
@Authenticated
public class FunctionalController extends Controller{
  
  public Result upload() {
    MultipartFormData body = request().body().asMultipartFormData();
    MultipartFormData.FilePart typeFile = body.getFile("file");
    if(typeFile != null) {
      String fileName = typeFile.getFilename();
      String contentType = typeFile.getContentType();
      File file = typeFile.getFile();
      System.out.println("****File upload: "+fileName);
      return ok("File uploaded");
    } else {
      flash("Error","Missing file");
      return badRequest();
    }
  }
}
