/**
 * 
 */
package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.vm.VMModel;

import org.ats.cloudstack.CloudStackClient;
import org.ats.component.usersmgt.DataFactory;
import org.ats.knife.Knife;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 10, 2014
 */
public class VMHelper extends AbstractHelper {

  public static long vmCount() {
    DB vmDB = DataFactory.getDatabase(databaseName);
    return vmDB.getCollection(vmColumn).count();
  }
  
  public static boolean createVM(VMModel... vms) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(vmColumn);
    WriteResult result = col.insert(vms, WriteConcern.ACKNOWLEDGED);
    boolean exist = false;
    for (DBObject index : col.getIndexInfo()) {
      if ("VM Index".equals(index.get("name"))) exist = true;
    }
    if (!exist) {
      col.ensureIndex(new BasicDBObject("name", "text"), "VM Index");
      System.out.println("Create VM Index");
    }
    return result.getError() == null;
  }
  
  public static VMModel updateVM(VMModel vm) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(vmColumn);
    col.save(vm);
    return vm;
  }
  
  public static boolean removeVM(VMModel vm) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(vmColumn);
    WriteResult result = col.remove(vm);
    return result.getError() == null;
  }
  
  public static List<VMModel> getVMsByGroupID(String groupId) {
    DB vmDB = getDatabase();
    DBCursor cursor = vmDB.getCollection(vmColumn).find(new BasicDBObject("group_id", groupId));
    List<VMModel> vms = new ArrayList<VMModel>();
    while (cursor.hasNext()) {
      vms.add(new VMModel().from(cursor.next()));
    }
    Collections.sort(vms, new Comparator<VMModel>() {
      @Override
      public int compare(VMModel o1, VMModel o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return vms;
  }
  
  public static List<VMModel> getVMsByGroupID(String groupId, DBObject filter) {
    filter.put("group_id", groupId);
    DB vmDB = getDatabase();
    DBCursor cursor = vmDB.getCollection(vmColumn).find(filter);
    List<VMModel> vms = new ArrayList<VMModel>();
    while (cursor.hasNext()) {
      vms.add(new VMModel().from(cursor.next()));
    }
    Collections.sort(vms, new Comparator<VMModel>() {
      @Override
      public int compare(VMModel o1, VMModel o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return vms;
  }
  
  public static VMModel getVMByID(String vmId) {
    DB vmDB = getDatabase();
    DBCursor cursor = vmDB.getCollection(vmColumn).find(new BasicDBObject("_id", vmId));
    return cursor.hasNext() ? new VMModel().from(cursor.next()) : null;
  }
  
  public static List<VMModel> getVMs(DBObject filter) {
    DB vmDB = getDatabase();
    DBCursor cursor = vmDB.getCollection(vmColumn).find(filter);
    List<VMModel> list = new ArrayList<VMModel>();
    while (cursor.hasNext()) {
      list.add(new VMModel().from(cursor.next()));
    }
    return list;
  }
  
  public static void setSystemProperties(Map<String, String> properties) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(propertiesColumn);
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      col.insert(BasicDBObjectBuilder.start("_id", entry.getKey()).append("value", entry.getValue()).append("system_property", true).get());
    }
  }
  
  public static Map<String, String> getSystemProperties() {
    DB db = getDatabase();
    DBCollection col = db.getCollection(propertiesColumn);
    DBCursor cursor = col.find(new BasicDBObject("system_property", true));
    Map<String, String> map = new HashMap<String, String>();
    while (cursor.hasNext()) {
      DBObject current = cursor.next();
      map.put((String)current.get("_id"), (String)current.get("value"));
    }
    
    return map;
  }
  
  public static String getSystemProperty(String name) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(propertiesColumn);
    DBObject obj = col.findOne(new BasicDBObject("_id", name));
    return (String) obj.get("value");
  }
  
  public static CloudStackClient getCloudStackClient() {
    Map<String, String> properties = getSystemProperties();
    String cloudstackApiUrl = properties.get("cloudstack-api-url");
    String cloudstackApiKey = properties.get("cloudstack-api-key");
    String cloudstackApiSecret = properties.get("cloudstack-api-secret");
    return new CloudStackClient(cloudstackApiUrl, cloudstackApiKey, cloudstackApiSecret);
  }
  
  public static Knife getKnife() {
    DB db = getDatabase();
    DBCollection col = db.getCollection(vmColumn);
    VMModel chefWorkstation = new VMModel().from(col.findOne(BasicDBObjectBuilder.start("system", true).add("name", "chef-workstation").get()));
    Knife knife = new Knife(chefWorkstation.getPublicIP(), chefWorkstation.getUsername(), chefWorkstation.getPassword());
    return knife;
  }
}
