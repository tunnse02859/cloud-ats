/**
 * 
 */
package helpertest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import models.test.TestProjectModel;
import models.test.TestProjectModel.TestProjectType;

import org.ats.component.usersmgt.DataFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

import controllers.Application;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class TestHelper {

  public static DB getDatabase() {
    return DataFactory.getDatabase(Application.dbName);
  }
  
  public static boolean createProject(TestProjectType type, TestProjectModel... projects) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(type.toString());
    WriteResult result = col.insert(projects, WriteConcern.ACKNOWLEDGED);
    boolean exist = false;
    for (DBObject index : col.getIndexInfo()) {
      if ((type + " Index").equals(index.get("name"))) exist = true;
    }
    if (!exist) {
      col.ensureIndex(new BasicDBObject("name", "text"), type + " Index");
      System.out.println("Create " + type +" Index");
    }
    return result.getError() == null;
  }
  
  public static TestProjectModel updateProject(TestProjectModel project) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(project.getType().toString());
    col.save(project);
    return project;
  }
  
  public static boolean removeProject(TestProjectModel project) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(project.getType().toString());
    WriteResult result = col.remove(project);
    return result.getError() == null;
  }
  
  public static List<TestProjectModel> getProject(TestProjectType type, DBObject query) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(type.toString());
    DBCursor cursor = col.find(query);
    
    List<TestProjectModel> holder = new ArrayList<TestProjectModel>();
    while (cursor.hasNext()) {
      TestProjectModel project = new TestProjectModel();
      project.from(cursor.next());
      holder.add(project);
    }
    return holder;
  }
  
  public static TestProjectModel getProjectById(TestProjectType type, String projectId) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(type.toString());
    DBObject source = col.findOne(new BasicDBObject("_id", projectId));
    return new TestProjectModel().from(source);
  }
  
  public static int getCurrentProjectIndex(TestProjectType type) throws IOException {
    DB db = getDatabase();
    DBCollection col = db.getCollection(type.toString());    
    DBCursor cursor = col.find().sort(new BasicDBObject("index", -1)).limit(1);
    
    if (cursor.hasNext()) return 0;
    DBObject obj = cursor.next();
    return obj.get("index") != null ?  (Integer) obj.get("index") : 0;
  }
}
