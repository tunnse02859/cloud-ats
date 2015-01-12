/**
 * 
 */
package helpertest;

import java.util.ArrayList;
import java.util.List;

import models.test.TestProjectModel;

import org.ats.component.usersmgt.DataFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;

import controllers.Application;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class TestProjectHelper {
  
  public static final String column = "test_project";

  public static DB getDatabase() {
    return DataFactory.getDatabase(Application.dbName);
  }
  
  public static DBCollection getCollection() {
    DB db = getDatabase();
    return db.getCollection(column);
  }
  
  public static void createProject(TestProjectModel... projects) {
    DBCollection col = getCollection();
    col.insert(projects, WriteConcern.ACKNOWLEDGED);

    //create index
    boolean exist = false;
    for (DBObject index : col.getIndexInfo()) {
      if ("Test Project Index".equals(index.get("name"))) exist = true;
    }
    if (!exist) {
      col.createIndex(new BasicDBObject("name", "text"));
      System.out.println("Create Test Project Index");
    }
  }
  
  public static TestProjectModel updateProject(TestProjectModel project) {
    DBCollection col = getCollection();
    col.save(project);
    return project;
  }
  
  public static void removeProject(TestProjectModel project) {
    DBCollection col = getCollection();
    col.remove(project);
  }
  
  public static List<TestProjectModel> getProject(DBObject query) {
    DBCollection col = getCollection();
    DBCursor cursor = col.find(query);
    
    List<TestProjectModel> holder = new ArrayList<TestProjectModel>();
    while (cursor.hasNext()) {
      TestProjectModel project = new TestProjectModel();
      project.from(cursor.next());
      holder.add(project);
    }
    return holder;
  }
  
  public static TestProjectModel  getProjectById(String projectId) {
    DBCollection col = getCollection();
    DBObject source = col.findOne(new BasicDBObject("_id", projectId));
    return source == null ? null : new TestProjectModel().from(source);
  }
  
}
