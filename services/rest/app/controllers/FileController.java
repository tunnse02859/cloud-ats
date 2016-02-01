/**
 * TrinhTV3@fsoft.com.vn
 */
package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

import org.ats.service.blob.FileService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.performance.CSV;
import org.ats.services.performance.JMeterScript;
import org.ats.services.performance.JMeterScriptService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;
import actions.CorsComposition;
import au.com.bytecode.opencsv.CSVReader;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * @author TrinhTV3
 *
 */

@CorsComposition.Cors
@Authenticated
public class FileController extends Controller {
  
  @Inject private FileService fileService;
  
  public Result list(String scriptId) {
    
    List<GridFSDBFile> listFile = fileService.find(new BasicDBObject("script_id", scriptId));
    ArrayNode array = Json.newObject().arrayNode();
    for (GridFSDBFile f : listFile) {
      ObjectNode obj = Json.newObject();
      obj.put("name", f.getId().toString());
      array.add(obj);
    }
    return ok(Json.parse(array.toString()));
  }
  
  public Result uploadCSVData(String scriptId) throws IOException {
    
    MultipartFormData body = request().body().asMultipartFormData();
    File file = body.getFile("file").getFile();
    String fileName = body.getFile("file").getFilename();
      
    GridFSInputFile fileFs = fileService.create(file);
    fileFs.put("_id", UUID.randomUUID().toString());
    fileFs.put("filename", fileName);
    fileFs.put("script_id", scriptId);
    fileService.save(fileFs);

//    JMeterScript script = scriptService.get(scriptId);
//    List<GridFSDBFile> files = fileService.find(new BasicDBObject("script_id", scriptId));
//    for (GridFSDBFile f : files) {
//      script.addCSVFiles(new CSV(f.getId().toString(), f.getFilename()));
//    }
//    scriptService.update(script);

    ObjectNode object = Json.newObject();
    object.put("name", fileName);
    object.put("_id", fileFs.getId().toString());
//    if (fileService.query(new BasicDBObject("_id", fileName)).size() > 0) {
//      fileService.delete(new BasicDBObject("_id", fileName));
//    }
//   
//    
    
    return ok(object);
    
  }
  
  public Result getCSVData(String scriptId, String csvId) throws IOException {
    
    GridFSDBFile file = fileService.findOne(new BasicDBObject("_id", csvId+"_temp")) == null ?  fileService.findOne(new BasicDBObject("_id", csvId)) : fileService.findOne(new BasicDBObject("_id", csvId+"_temp"));
    BufferedReader buffer = new BufferedReader(new InputStreamReader(file.getInputStream()));
    ArrayNode array = readBufferByOpenCSV(buffer);
    
    return ok(array);
  }
  
  public Result deleteCSVData(String scriptId, String csvId) {
    
    fileService.deleteById(csvId);
    return ok();
  }
  
  public Result updateTempCSVData(String scriptId, String csvId) throws IOException {
    
    MultipartFormData body = request().body().asMultipartFormData();
    
    String fileName = body.asFormUrlEncoded().get("filename")[0];
    File file = body.getFile("file").getFile();
    fileService.deleteById(csvId+"_temp");
    GridFSInputFile fileFs = fileService.create(file);
    fileFs.put("_id", csvId+"_temp");
    fileFs.put("script_id", scriptId);
    fileFs.put("filename", fileName);
    
    fileService.save(fileFs);
    return ok();
  }
  
  public Result deleteTempCSVData(String scriptId) {
    
    List<GridFSDBFile> list = fileService.find(new BasicDBObject("script_id", scriptId));
    
    for (GridFSDBFile file : list) {
      if (file.getId().toString().contains("_temp")) {
        fileService.deleteById(file.getId().toString());
      }
    }
    return ok();
  }
  @SuppressWarnings("resource")
  private ArrayNode readBufferByOpenCSV(BufferedReader br) {
    ArrayNode array = null;
    try {
      array = Json.newObject().arrayNode();
      CSVReader reader = new CSVReader(br);
      String[] nextLine;
      
      while ((nextLine = reader.readNext()) != null) {
        ObjectNode object = Json.newObject();
        for (int i = 0; i < nextLine.length; i++) {
          object.put("Col " + i, nextLine[i]);
        }
        array.add(object);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return array;

  }
  
}
