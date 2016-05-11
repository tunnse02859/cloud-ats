/**
 * 
 */
package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.ats.common.MapBuilder;
import org.ats.common.PageList;
import org.ats.services.OrganizationContext;
import org.ats.services.datadriven.DataDriven;
import org.ats.services.datadriven.DataDrivenFactory;
import org.ats.services.datadriven.DataDrivenReference;
import org.ats.services.datadriven.DataDrivenService;
import org.ats.services.keyword.Case;
import org.ats.services.keyword.CaseService;
import org.ats.services.organization.SpaceService;
import org.ats.services.organization.acl.Authenticated;
import org.ats.services.organization.entity.User;
import org.ats.services.organization.entity.fatory.ReferenceFactory;
import org.ats.services.organization.entity.reference.SpaceReference;
import org.ats.services.project.MixProject;
import org.ats.services.project.MixProjectService;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;
import actions.CorsComposition;
import au.com.bytecode.opencsv.CSVReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * May 21, 2015
 */
@CorsComposition.Cors
@Authenticated
public class DataDrivenController extends Controller {
  
  @Inject
  OrganizationContext context;
  
  @Inject
  SpaceService spaceService;
  
  @Inject
  ReferenceFactory<SpaceReference> spaceRefFactory;
  
  @Inject
  DataDrivenService dataDrivenService;
  
  @Inject
  DataDrivenFactory dataDrivenFactory;
  
  @Inject
  ReferenceFactory<DataDrivenReference> dataRefFactory;
  
  @Inject 
  MixProjectService mpService;
  
  @Inject
  CaseService caseService;
  
  public Result list(String projectId) {
    
    MixProject mp = mpService.get(projectId);
    
    PageList<DataDriven> pages = dataDrivenService.query(new BasicDBObject("mix_id", projectId));
    pages.setSortable(new MapBuilder<String, Boolean>("created_date", false).build());
    
    BasicDBList array = new BasicDBList();
    while (pages.hasNext()) {
      for (DataDriven data : pages.next()) {
        User user = data.getCreator().get();
        BasicDBObject userObj = new BasicDBObject("email", user.getEmail()).append("first_name", user.getFirstName()).append("last_name", user.getLastName());
        BasicDBObject obj = new BasicDBObject();
        obj.put("_id", data.getId());
        obj.put("name", data.getName());
        obj.put("creator", userObj);
        obj.put("created_date", data.getDate("created_date").getTime());
        ArrayNode dataSource = (ArrayNode) Json.parse(data.getDataSource());
        int row = dataSource.size();
        obj.put("row", row);
        if (row > 0) {
          JsonNode node = dataSource.get(0);
          obj.put("column", node.size());
        }
        array.add(obj);
      }
    }
    
    mp.put("datum", array);
    
    return ok(Json.parse(mp.toString()));
  }
  
  public Result create(String projectId) {
    JsonNode json = request().body().asJson();
    String name = json.get("name").asText();
    String caseId = json.get("caseId").asText();
    JsonNode dataset = json.get("dataset");
    DataDriven driven = dataDrivenFactory.create(projectId, name, dataset.toString());
    dataDrivenService.create(driven);
    
    if ("null".equals(caseId)) {
      return status(200, Json.parse(driven.toString()));
    }
    
    String drivenId = driven.getId();
    Case caze = caseService.get(caseId);
    
    caze.setDataDriven(dataRefFactory.create(drivenId));
    caseService.update(caze);
    return ok(Json.parse(driven.toString()));
  }
  
  public Result get(String id) {
    DataDriven driven = dataDrivenService.get(id);
    String projectId = driven.getString("mix_id");
    MixProject mp = mpService.get(projectId);
    driven.put("projectName", mp.getName());
    return ok(Json.parse(driven.toString()));
  }
  
  public Result delete() {
    
    String id = request().body().asText();
    DataDrivenReference dataRef = dataRefFactory.create(id);
    
    PageList<Case> pages = caseService.query(new BasicDBObject("data_driven", dataRef.toJSon()));
    while (pages.hasNext()) {
      List<Case> list = pages.next();
      for (Case caze : list) {
        caze.setDataDriven(null);
        caseService.update(caze);
      }
    }
    
    dataDrivenService.delete(id);
    
    return status(200);
  }
  
  public Result update() {
    JsonNode json = request().body().asJson();
    String id = json.get("id").asText();
    JsonNode dataset = json.get("dataset");
    DataDriven driven = dataDrivenService.get(id);
    String name = json.get("name").asText();
    if (dataset.toString().equals(driven.getDataSource()) && name.equals(driven.getName())) {
      return status(304);
    }
    driven.put("name", name);
    driven.setDataSource(dataset.toString());
    dataDrivenService.update(driven);
    
    return status(200, Json.parse(driven.toString()));
  }
  
  public Result upload(String projectId,String caseId) throws FileNotFoundException {
    
    MultipartFormData body = request().body().asMultipartFormData();
    MultipartFormData.FilePart typeFile = body.getFile("file");
    String fileName = typeFile.getFilename();
    File file = typeFile.getFile();
    
    BufferedReader br = new BufferedReader(new FileReader(file));
    ArrayNode array = readBufferByOpenCSV(br);
    DataDriven data = dataDrivenFactory.create(projectId, fileName, array.toString());
    
    if ("null".equals(projectId)) {
      return ok(Json.parse(data.toString()));
    } else {
      dataDrivenService.create(data);
    }
 
    if ("null".equals(caseId)) {
      return status(200, Json.parse(data.toString()));
    }
        
    String drivenId = data.getId();
    Case caze = caseService.get(caseId);

    if(caze.getDataDriven() != null) {
      caze.setDataDriven(null);
    }

    caze.setDataDriven(dataRefFactory.create(drivenId));
    caseService.update(caze);
    
    return ok(Json.parse(data.toString()));
  }
  @SuppressWarnings("resource")
  private ArrayNode readBufferByOpenCSV(BufferedReader br) {
    ArrayNode array = null;
    try {
      array = Json.newObject().arrayNode();
      CSVReader reader = new CSVReader(br);
      String[] nextLine;
      String[] firstLine = reader.readNext();
      while ((nextLine = reader.readNext()) != null) {
        ObjectNode object = Json.newObject();
        for (int i = 0; i < nextLine.length; i++) {
          object.put(firstLine[i], nextLine[i]);
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

