/**
 * 
 */
package org.ats.services.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Before;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 *
 * Apr 1, 2015
 */
public class MapReduceTestCase {
  
  private MongoDBService dbService;

  @Before
  public void init() throws Exception {
    System.setProperty(DatabaseModule.DB_CONF, "");
    Injector injector = Guice.createInjector(new DatabaseModule());
    dbService = injector.getInstance(MongoDBService.class);
    dbService.dropDatabase();
  }
  
  private void initData(int size) {
    BasicDBObject tenant = new BasicDBObject("_id", "fsoft");
    tenant.append("name", "FPT Software");
    
    DBCollection col = dbService.getDatabase().getCollection("tenant");
    col.insert(tenant);
    
    col = dbService.getDatabase().getCollection("space");
    List<DBObject> batch = new ArrayList<DBObject>(1000);
    for (int i = 0; i < size; i++) {
      BasicDBObject space = new BasicDBObject("_id", "space" + i);
      space.append("tenant", "fsoft");
      space.append("name", "fsoft space");
      batch.add(space);
      if (batch.size() == 1000) {
        col.insert(batch);
        batch.clear();
      }
    }
    
    if (batch.size() > 0) {
      col.insert(batch);
      batch.clear();
    }
    
    col = dbService.getDatabase().getCollection("user");
    for (int i = 0; i < size; i++) {
      BasicDBObject user = new BasicDBObject("_id", "user" + i);
      user.put("tenant", "fsoft");
      user.put("space", "space" + new Random().nextInt(size));
      user.put("name", "fsoft user name");
      batch.add(user);
      if (batch.size() == 1000) {
        col.insert(batch);
        batch.clear();
      }
    }
    
    if (batch.size() > 0) {
      col.insert(batch);
      batch.clear();
    }
  }
  
  //@Test
  public void testInActiveTenantByMapReducePrototype() {
    long start = System.currentTimeMillis();
    initData(1000 * 1000);
    System.out.println("Init 1m spaces and 1m users in " + (System.currentTimeMillis() - start) / 1000.0 + "(s)");
    
    start = System.currentTimeMillis();
    
    DBCollection inactive = dbService.getDatabase().getCollection("inactive_tenant");
    DBCollection tenantCol = dbService.getDatabase().getCollection("tenant");
    DBCollection spaceCol = dbService.getDatabase().getCollection("space");
    DBCollection userCol = dbService.getDatabase().getCollection("user");
    
    DBObject tenant = tenantCol.findOne();
    inactive.insert(new BasicDBObject("_id", tenant.get("_id")).append("value", new BasicDBObject("tenant", tenant)));
    tenantCol.remove(tenant);

    String map ="function () { emit(this._id, { space : this } ); }";
    String reduce = "function (key, values) { return values[0]; }";
    
    BasicDBObject query = new BasicDBObject("tenant", "fsoft");
    MapReduceCommand cmd = new MapReduceCommand(spaceCol, map, reduce, "inactive_tenant", OutputType.MERGE, query);
    spaceCol.mapReduce(cmd);
    spaceCol.remove(query);

    map ="function () { emit(this._id, { user : this } ); }";
    cmd = new MapReduceCommand(userCol, map, reduce, "inactive_tenant", OutputType.MERGE, query);
    userCol.mapReduce(cmd);
    userCol.remove(query);
    System.out.println("Process inactive tenant by mapreduce in " + (System.currentTimeMillis() - start) / 1000.0 + "(s)");
    
    Assert.assertEquals(0, tenantCol.count());
    Assert.assertEquals(0, spaceCol.count());
    Assert.assertEquals(0, userCol.count());
    Assert.assertEquals(2000 * 1000 + 1, inactive.count());
  }
  
 // @Test
  public void testInActiveTenantByBatchPrototype() {
    long start = System.currentTimeMillis();
    initData(1000 * 1000);
    System.out.println("Init 1m spaces and 1m users in " + (System.currentTimeMillis() - start) / 1000.0 + "(s)");
    
    start = System.currentTimeMillis();
    
    DBCollection inactive = dbService.getDatabase().getCollection("inactive_tenant");
    DBCollection tenantCol = dbService.getDatabase().getCollection("tenant");
    DBCollection spaceCol = dbService.getDatabase().getCollection("space");
    DBCollection userCol = dbService.getDatabase().getCollection("user");
    
    DBObject tenant = tenantCol.findOne();
    inactive.insert(new BasicDBObject("_id", tenant.get("_id")).append("value", new BasicDBObject("tenant", tenant)));
    tenantCol.remove(tenant);
    
    BasicDBObject query = new BasicDBObject("tenant", "fsoft");
    DBCursor cursor = spaceCol.find(query);
    
    List<DBObject> batch = new ArrayList<DBObject>(1000);
    
    while (cursor.hasNext()) {
      DBObject obj = cursor.next();
      batch.add(new BasicDBObject("_id", obj.get("_id")).append("value", new BasicDBObject("space", obj)));
      if (batch.size() == 1000) {
        inactive.insert(batch);
        batch.clear();
      }
    }
    
    if (batch.size() > 0) {
      inactive.insert(batch);
      batch.clear();
    }
    spaceCol.remove(query);
    
    query = new BasicDBObject("tenant", "fsoft");
    cursor = userCol.find(query);
    batch = new ArrayList<DBObject>(1000);
    
    while (cursor.hasNext()) {
      DBObject obj = cursor.next();
      batch.add(new BasicDBObject("_id", obj.get("_id")).append("value", new BasicDBObject("user", obj)));
      if (batch.size() == 1000) {
        inactive.insert(batch);
        batch.clear();
      }
    }
    
    if (batch.size() > 0) {
      inactive.insert(batch);
      batch.clear();
    }
    userCol.remove(query);
    
    System.out.println("Process inactive tenant by batch in " + (System.currentTimeMillis() - start) / 1000.0 + "(s)");
    
    Assert.assertEquals(0, tenantCol.count());
    Assert.assertEquals(0, spaceCol.count());
    Assert.assertEquals(0, userCol.count());
    Assert.assertEquals(2000 * 1000 + 1, inactive.count());
  }
  
}
