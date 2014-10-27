/**
 * 
 */
package helpertest;

import java.util.ArrayList;
import java.util.List;

import org.ats.component.usersmgt.DataFactory;
import org.ats.jmeter.models.JMeterScript;

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
 * Oct 24, 2014
 */
public class JMeterScriptHelper {
  
  public static DB getDatabase() {
    return DataFactory.getDatabase(Application.dbName);
  }
  
  public static boolean createScript(JMeterScript... scripts) {
    DB db = getDatabase();
    DBCollection col = db.getCollection("jmeter");
    WriteResult result = col.insert(scripts, WriteConcern.ACKNOWLEDGED);
    return result.getError() == null;
  }
  
  public static JMeterScript updateJMeterScript(JMeterScript script) {
    DB db = getDatabase();
    DBCollection col = db.getCollection("jmeter");
    col.save(script);
    return script;
  }
  
  public static void deleteJMeterScript(String scriptId) {
    DB db = getDatabase();
    DBCollection col = db.getCollection("jmeter");
    col.remove(new BasicDBObject("_id", scriptId));
  }
  
  public static void deleteScriptOfProject(String projectId) {
    DB db = getDatabase();
    DBCollection col = db.getCollection("jmeter");
    col.remove(new BasicDBObject("project_id", projectId));
  }
  
  public static List<JMeterScript> getJMeterScript(String projectId) {
    DB db = getDatabase();
    DBCollection col = db.getCollection("jmeter");
    BasicDBObject query = new BasicDBObject("project_id", projectId);
    
    DBCursor cursor = col.find(query);
    List<JMeterScript> scripts = new ArrayList<JMeterScript>();
    while (cursor.hasNext()) {
      scripts.add(new JMeterScript().from(cursor.next()));
    }
    return scripts;
  }
  
  public static JMeterScript getJMeterScript(String projectId, String commit) {
    DB db = getDatabase();
    DBCollection col = db.getCollection("jmeter");
    BasicDBObject query = new BasicDBObject("project_id", projectId).append("commit", commit);
    
    DBCursor cursor = col.find(query);
    
    if (!cursor.hasNext()) return null;
    DBObject source = cursor.next();
    JMeterScript script = new JMeterScript().from(source);
    return script;
  }
  
  public static JMeterScript getJMeterScriptById(String scriptId) {
    DB db = getDatabase();
    DBCollection col = db.getCollection("jmeter");
    DBObject source = col.findOne(new BasicDBObject("_id", scriptId));
    return new JMeterScript().from(source);
  }
  
  public static JMeterScript getLastestCommit(String projectId) {
    DB db = getDatabase();
    DBCollection col = db.getCollection("jmeter");
    BasicDBObject query = new BasicDBObject("project_id", projectId);
    
    DBCursor cursor = col.find(query).sort(new BasicDBObject("index", -1));
    
    List<JMeterScript> scripts = new ArrayList<JMeterScript>();
    while (cursor.hasNext()) {
      scripts.add(new JMeterScript().from(cursor.next()));
    }
    return scripts.isEmpty() ? null : scripts.get(0);
  }
}
