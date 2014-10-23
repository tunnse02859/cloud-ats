/**
 * 
 */
package helpertest;

import models.test.TestProjectModel;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Oct 22, 2014
 */
public class PerformanceHelper extends AbstractHelper {

  public static boolean createPerformanceProject(TestProjectModel... projects) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(performanceColumn);
    WriteResult result = col.insert(projects, WriteConcern.ACKNOWLEDGED);
    boolean exist = false;
    for (DBObject index : col.getIndexInfo()) {
      if ("Performance Index".equals(index.get("name"))) exist = true;
    }
    if (!exist) {
      col.ensureIndex(new BasicDBObject("name", "text"), "Performance Index");
      System.out.println("Create Performance Index");
    }
    return result.getError() == null;
  }
  
  public static TestProjectModel updatePerformanceProject(TestProjectModel project) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(performanceColumn);
    col.save(project);
    return project;
  }
  
  public static boolean removePerformanceProject(TestProjectModel project) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(performanceColumn);
    WriteResult result = col.remove(project);
    return result.getError() == null;
  }
}
