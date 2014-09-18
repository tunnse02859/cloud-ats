/**
 * 
 */
package utils;

import java.util.ArrayList;
import java.util.List;

import models.vm.OfferingModel;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Sep 18, 2014
 */
public class OfferingHelper extends AbstractHelper {
  
  public static void addOfferingGroup(String groupId, OfferingModel offering) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(groupOfferingColumn);
    offering.put("group_id", groupId);
    col.insert(offering, WriteConcern.ACKNOWLEDGED);
  }
  
  public static boolean createOffering(OfferingModel... offerings) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(offeringColumn);
    WriteResult result = col.insert(offerings, WriteConcern.ACKNOWLEDGED);
    return result.getError() == null;
  }
  
  public static OfferingModel updateOffering(OfferingModel offering) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(offeringColumn);
    col.save(offering);
    return offering;
  }
  
  public static boolean removeOffering(OfferingModel offering) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(offeringColumn);
    WriteResult result = col.remove(offering);
    return result.getError() == null;
  }
  
  public static OfferingModel getOffering(String id) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(offeringColumn);
    DBObject source = col.findOne(new BasicDBObject("_id", id));
    return source == null ? new OfferingModel() : new OfferingModel().from(source);
  }
  
  public static List<OfferingModel> getEnableOfferings() {
    return getOfferings(new BasicDBObject("disabled", true));
  }
  
  public static List<OfferingModel> getOfferings() {
    return getOfferings(new BasicDBObject());
  }
  
  public static OfferingModel getDefaultOfferingOfGroup(String groupId) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(groupOfferingColumn);
    DBObject source = col.findOne(new BasicDBObject("group_id", groupId));
    return source == null ? new OfferingModel() : new OfferingModel().from(source);
  }
  
  public static List<OfferingModel> getOfferings(BasicDBObject filter) {
    DB db = getDatabase();
    DBCollection col = db.getCollection(offeringColumn);
    DBCursor cusor = col.find(filter);
    List<OfferingModel> list = new ArrayList<OfferingModel>();
    while (cusor.hasNext()) {
      list.add(new OfferingModel().from(cusor.next()));
    }
    return list;
  }
}
